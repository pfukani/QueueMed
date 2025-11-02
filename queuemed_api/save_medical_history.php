<?php
header("Content-Type: application/json");
require 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    $patientDetails = $data['patient_details'] ?? '';
    $diagnosedConditions = $data['diagnosed_conditions'] ?? '';
    $hereditaryDiseases = $data['hereditary_diseases'] ?? '';
    $allergies = $data['allergies'] ?? '';
    $vaccinationStatus = $data['vaccination_status'] ?? '';
    $sexualHistory = $data['sexual_history'] ?? '';
    $notes = $data['notes'] ?? '';
    $createdBy = $data['created_by'] ?? null;
    
    if (empty($patientDetails)) {
        echo json_encode(["success" => false, "message" => "Patient details are required"]);
        exit;
    }
    
    if (!$createdBy) {
        echo json_encode(["success" => false, "message" => "Staff ID is required"]);
        exit;
    }
    
    try {
        $stmt = $conn->prepare("INSERT INTO medical_history 
            (patient_details, diagnosed_conditions, hereditary_diseases, allergies, vaccination_status, sexual_history, notes, created_by) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        
        $stmt->bind_param("sssssssi", $patientDetails, $diagnosedConditions, $hereditaryDiseases, $allergies, $vaccinationStatus, $sexualHistory, $notes, $createdBy);
        
        if ($stmt->execute()) {
            echo json_encode([
                "success" => true, 
                "message" => "Medical history saved successfully", 
                "history_id" => $stmt->insert_id,
                "patient_details" => $patientDetails
            ]);
        } else {
            throw new Exception("Failed to save medical history: " . $stmt->error);
        }
        
        $stmt->close();
    } catch (Exception $e) {
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
    
    $conn->close();
}
?>