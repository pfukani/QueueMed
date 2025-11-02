<?php
require 'db.php';

error_reporting(E_ALL);
ini_set('display_errors', 1);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $id = $_POST['id'];
    $first_name = $_POST['first_name'];
    $last_name = $_POST['last_name'];
    $id_number = $_POST['id_number'];
    $sex = $_POST['sex'];
    $age = $_POST['age'];
    $contact_no = $_POST['contact_no'];
    $race = $_POST['race'];
    $language = $_POST['language'];
    $email = $_POST['email'];

    var_dump($_POST);

    try {
        $stmt = $conn->prepare("UPDATE users 
            SET first_name=?, last_name=?, id_number=?, sex=?, age=?, contact_no=?, race=?, language=?, email=? 
            WHERE id=?");
        if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);

        $stmt->bind_param("ssssissssi", 
            $first_name, $last_name, $id_number, $sex, $age, $contact_no, $race, $language, $email, $id);

        if (!$stmt->execute()) {
            throw new Exception("Execute failed: " . $stmt->error);
        }
        
        $stmt->close();

        // Fetch the updated user data to return
        $updatedStmt = $conn->prepare("SELECT first_name, last_name, id_number, sex, age, contact_no, race, language, email, image_url FROM users WHERE id = ?");
        $updatedStmt->bind_param("i", $id);
        $updatedStmt->execute();
        $updatedStmt->bind_result($updated_first_name, $updated_last_name, $updated_id_number, $updated_sex, $updated_age, $updated_contact_no, $updated_race, $updated_language, $updated_email, $updated_image_url);
        $updatedStmt->fetch();
        $updatedStmt->close();

        echo json_encode([
            "status" => "success", 
            "message" => "Profile updated successfully",
            "updated_data" => [
                "first_name" => $updated_first_name,
                "last_name" => $updated_last_name,
                "id_number" => $updated_id_number,
                "sex" => $updated_sex,
                "age" => $updated_age,
                "contact_no" => $updated_contact_no,
                "race" => $updated_race,
                "language" => $updated_language,
                "email" => $updated_email,
                "image_url" => $updated_image_url
            ]
        ]);

    } catch (Exception $e) {
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }

    $conn->close();
}
?>