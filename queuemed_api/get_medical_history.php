<?php
header("Content-Type: application/json");
require 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $historyId = $_GET['history_id'] ?? null;
    
    if (!$historyId) {
        echo json_encode(["success" => false, "message" => "History ID is required"]);
        exit;
    }
    
    try {
        $stmt = $conn->prepare("
            SELECT id, patient_details, diagnosed_conditions, hereditary_diseases, 
                   allergies, vaccination_status, sexual_history, notes, created_at
            FROM medical_history 
            WHERE id = ?
        ");
        
        $stmt->bind_param("i", $historyId);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows > 0) {
            $history = $result->fetch_assoc();
            echo json_encode(["success" => true, "data" => $history]);
        } else {
            echo json_encode(["success" => false, "message" => "Medical history not found"]);
        }
        
        $stmt->close();
    } catch (Exception $e) {
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
    
    $conn->close();
}
?>