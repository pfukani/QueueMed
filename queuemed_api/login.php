<?php
header("Content-Type: application/json");

// Database connection on custom port 3307
$conn = new mysqli("localhost", "root", "", "queuemed", 3307);
if($conn->connect_error){
    die(json_encode([
        "success" => false, 
        "message" => "Database connection failed: " . $conn->connect_error
    ]));
}

// Get JSON body
$data = json_decode(file_get_contents("php://input"), true);

$email = $data['email'] ?? '';
$password = $data['password'] ?? '';

if(empty($email) || empty($password)){
    echo json_encode(["success"=>false, "message"=>"Please fill all fields"]);
    exit;
}

// Find user
$stmt = $conn->prepare("SELECT id, first_name, email, contact, password, role FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();
$stmt->bind_result($id, $first_name, $emailDB, $contact, $hashedPassword, $role);

if($stmt->num_rows == 0){
    echo json_encode(["success"=>false, "message"=>"User not found"]);
    exit;
}

$stmt->fetch();

// Verify password
if(password_verify($password, $hashedPassword)){
    echo json_encode([
        "success" => true,
        "message" => "Login successful",
        "data" => [
            "first_name" => $first_name,
            "email" => $emailDB,
            "contact" => $contact,
            "role" => $role
        ]
    ]);
} else {
    echo json_encode(["success"=>false, "message"=>"Incorrect password"]);
}

$stmt->close();
$conn->close();
?>
