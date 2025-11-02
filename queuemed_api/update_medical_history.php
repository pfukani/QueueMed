<?php
header("Content-Type: application/json");
require 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    $historyId = $data['history_id'] ?? null;
    $patientDetails = $data['patient_details'] ?? '';
    $diagnosedConditions = $data['diagnosed_conditions'] ?? '';
    $hereditaryDiseases = $data['hereditary_diseases'] ?? '';
    $allergies = $data['allergies'] ?? '';
    $vaccinationStatus = $data['vaccination_status'] ?? '';
    $sexualHistory = $data['sexual_history'] ?? '';
    $notes = $data['notes'] ?? '';
    $staffId = $data['staff_id'] ?? null;
    
    if (empty($patientDetails)) {
        echo json_encode(["success" => false, "message" => "Patient details are required"]);
        exit;
    }
    
    if (!$historyId || !$staffId) {
        echo json_encode(["success" => false, "message" => "History ID and Staff ID are required"]);
        exit;
    }
    
    try {
        // Verify the history belongs to the staff before updating
        $checkStmt = $conn->prepare("SELECT id FROM medical_history WHERE id = ? AND created_by = ?");
        $checkStmt->bind_param("ii", $historyId, $staffId);
        $checkStmt->execute();
        $checkResult = $checkStmt->get_result();
        
        if ($checkResult->num_rows === 0) {
            echo json_encode(["success" => false, "message" => "Medical history not found or you don't have permission to update it"]);
            exit;
        }
        $checkStmt->close();
        
        // Update the medical history
        $stmt = $conn->prepare("UPDATE medical_history 
            SET patient_details = ?, diagnosed_conditions = ?, hereditary_diseases = ?, 
                allergies = ?, vaccination_status = ?, sexual_history = ?, notes = ?
            WHERE id = ? AND created_by = ?");
        
        $stmt->bind_param("sssssssii", $patientDetails, $diagnosedConditions, $hereditaryDiseases, 
                         $allergies, $vaccinationStatus, $sexualHistory, $notes, $historyId, $staffId);
        
        if ($stmt->execute()) {
            if ($stmt->affected_rows > 0) {
                echo json_encode(["success" => true, "message" => "Medical history updated successfully"]);
            } else {
                echo json_encode(["success" => false, "message" => "No changes were made"]);
            }
        } else {
            throw new Exception("Failed to update medical history: " . $stmt->error);
        }
        
        $stmt->close();
    } catch (Exception $e) {
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
    
    $conn->close();
}
?>