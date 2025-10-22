<?php
header("Content-Type: application/json");

// Database connection
$host = "localhost";
$port = 3307; // XAMPP MySQL port
$db = "queuemed";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db, $port);

if($conn->connect_error){
    die(json_encode([
        "success" => false, 
        "message" => "Database connection failed: " . $conn->connect_error
    ]));
}

// Get JSON body
$data = json_decode(file_get_contents("php://input"), true);

// Assign variables
$first = trim($data['first_name'] ?? '');
$last = trim($data['last_name'] ?? '');
$email = trim($data['email'] ?? '');
$contact = trim($data['contact'] ?? '');
$password = trim($data['password'] ?? '');

// Validate required fields
if(empty($first) || empty($last) || empty($email) || empty($password)){
    echo json_encode(["success" => false, "message" => "Please fill all fields"]);
    exit;
}

// Check if email already exists
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
if(!$stmt){
    die(json_encode(["success" => false, "message" => "Prepare failed: " . $conn->error]));
}

$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();

if($stmt->num_rows > 0){
    echo json_encode(["success" => false, "message" => "Email already registered"]);
    exit;
}
$stmt->close();

// Hash password
$hashed = password_hash($password, PASSWORD_DEFAULT);

// Insert user
$stmt = $conn->prepare("INSERT INTO users (first_name, last_name, email, contact, password) VALUES (?, ?, ?, ?, ?)");
if(!$stmt){
    die(json_encode(["success" => false, "message" => "Prepare failed: " . $conn->error]));
}

$stmt->bind_param("sssss", $first, $last, $email, $contact, $hashed);

if($stmt->execute()){
    echo json_encode(["success" => true, "message" => "Registration successful"]);
} else {
    echo json_encode(["success" => false, "message" => "Registration failed: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
