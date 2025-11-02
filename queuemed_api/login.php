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

// Find user - SELECT ALL FIELDS
$stmt = $conn->prepare("SELECT id, first_name, last_name, id_number, sex, age, contact_no, race, language, email, role, image_url, password_hash FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();
$stmt->bind_result($id, $first_name, $last_name, $id_number, $sex, $age, $contact, $race, $language, $emailDB, $role, $image_url, $hashedPassword);

if($stmt->num_rows == 0){
    echo json_encode(["success"=>false, "message"=>"User not found"]);
    exit;
}

$stmt->fetch();

// Verify password
if(password_verify($password, $hashedPassword)){
    // Return ALL user data
    echo json_encode([
        "success" => true,
        "message" => "Login successful",
        "data" => [
            "id" => $id,
            "first_name" => $first_name,
            "last_name" => $last_name,
            "id_number" => $id_number,
            "sex" => $sex,
            "age" => $age,
            "contact_no" => $contact,
            "race" => $race,
            "language" => $language,
            "email" => $emailDB,
            "role" => $role,
            "image_url" => $image_url
        ]
    ]);
} else {
    echo json_encode(["success"=>false, "message"=>"Incorrect password"]);
}

$stmt->close();
$conn->close();
?>