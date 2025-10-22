<?php
header("Content-Type: application/json");

// Database connection
$conn = new mysqli("localhost", "root", "", "queuemed", 3307);
if($conn->connect_error){
    die(json_encode(["success" => false, "message" => "Database connection failed."]));
}

$data = json_decode(file_get_contents("php://input"), true);
$email = $data['email'] ?? '';

if(empty($email)){
    echo json_encode(["success" => false, "message" => "Email is required"]);
    exit;
}

// Get appointments for this user
$stmt = $conn->prepare("SELECT patient_name, date, time FROM appointments WHERE patient_email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

$appointments = [];
while($row = $result->fetch_assoc()){
    $appointments[] = $row;
}

if(count($appointments) > 0){
    echo json_encode(["success" => true, "data" => $appointments]);
} else {
    echo json_encode(["success" => false, "message" => "No appointments found."]);
}

$stmt->close();
$conn->close();
?>
