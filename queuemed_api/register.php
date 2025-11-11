<?php
header("Content-Type: application/json");

// Database connection
$host = "localhost";
$port = 3307; // XAMPP MySQL port
$db = "queuemed";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db, $port);

if ($conn->connect_error) {
    die(json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error
    ]));
}

// Get JSON body
$data = json_decode(file_get_contents("php://input"), true);

// Assign variables safely
$first = trim($data['first_name'] ?? '');
$last = trim($data['last_name'] ?? '');
$email = trim($data['email'] ?? '');
$contact = trim($data['contact_no'] ?? '');
$password = trim($data['password'] ?? '');

//  Validate required fields
if (empty($first) || empty($last) || empty($email) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Please fill all fields"]);
    exit;
}

//  Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["success" => false, "message" => "Invalid email format"]);
    exit;
}

//  Strong password validation (minimum 8 chars, upper, lower, number, special char)
$pattern = "/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/";
if (!preg_match($pattern, $password)) {
    echo json_encode([
        "success" => false,
        "message" => "Password must be at least 8 characters long and include an uppercase letter, lowercase letter, number, and special character."
    ]);
    exit;
}

//  Check if email already exists
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
if (!$stmt) {
    die(json_encode(["success" => false, "message" => "Prepare failed: " . $conn->error]));
}

$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Email already registered"]);
    exit;
}
$stmt->close();

//  Hash password securely
$hashed = password_hash($password, PASSWORD_DEFAULT);

//  Insert user record
$stmt = $conn->prepare("INSERT INTO users (first_name, last_name, email, contact_no, password_hash) VALUES (?, ?, ?, ?, ?)");
if (!$stmt) {
    die(json_encode(["success" => false, "message" => "Prepare failed: " . $conn->error]));
}

$stmt->bind_param("sssss", $first, $last, $email, $contact, $hashed);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Registration successful"]);
} else {
    echo json_encode(["success" => false, "message" => "Registration failed: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
