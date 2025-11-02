<?php
header("Content-Type: application/json");
require 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $staffId = $_GET['staff_id'] ?? null;
    
    if (!$staffId) {
        echo json_encode(["success" => false, "message" => "Staff ID is required"]);
        exit;
    }
    
    try {
        $stmt = $conn->prepare("
            SELECT id, patient_details, diagnosed_conditions, hereditary_diseases, 
                   allergies, vaccination_status, sexual_history, notes, created_at
            FROM medical_history 
            WHERE created_by = ? 
            ORDER BY created_at DESC
        ");
        
        $stmt->bind_param("i", $staffId);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $histories = [];
        while ($row = $result->fetch_assoc()) {
            $histories[] = $row;
        }
        
        echo json_encode(["success" => true, "data" => $histories]);
        
        $stmt->close();
    } catch (Exception $e) {
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
    
    $conn->close();
}
?>