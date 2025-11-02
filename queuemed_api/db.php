<?php
$host = "localhost";
$port = 3307; // XAMPP MySQL port
$db = "queuemed";
$user = "root";
$pass = "";

// Include port in the host parameter
$conn = new mysqli($host . ":" . $port, $user, $pass, $db);

// Or use this alternative syntax:
// $conn = new mysqli($host, $user, $pass, $db, $port);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]));
}
?>