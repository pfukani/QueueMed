<?php
header("Content-Type: application/json");
include 'db.php';

$response = [];
$target_dir = "uploads/";

if (!isset($_FILES["image"])) {
    echo json_encode(["status" => "error", "message" => "No image file uploaded"]);
    exit;
}

$file_name = uniqid() . "_" . basename($_FILES["image"]["name"]);
$target_file = $target_dir . $file_name;

if (!is_dir($target_dir)) {
    mkdir($target_dir, 0777, true);
}

if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
    $image_url = "http://10.0.2.2/queuemed_api/" . $target_file; 

    // Get user ID from POST
    $user_id = isset($_POST['id']) ? intval($_POST['id']) : 0;
    
    if ($user_id > 0) {
        // Use prepared statement to prevent SQL injection
        $stmt = $conn->prepare("UPDATE users SET image_url = ? WHERE id = ?");
        if ($stmt) {
            $stmt->bind_param("si", $image_url, $user_id);
            if ($stmt->execute()) {
                $response["db_update"] = "User image updated successfully.";
            } else {
                $response["db_update_error"] = $stmt->error;
            }
            $stmt->close();
        } else {
            $response["db_update_error"] = $conn->error;
        }
    } else {
        $response["db_update_error"] = "No valid user ID provided. Received: " . $user_id;
    }

    $response["status"] = "success";
    $response["image_url"] = $image_url;
} else {
    $response["status"] = "error";
    $response["message"] = "Failed to upload image.";
}

echo json_encode($response);
?>