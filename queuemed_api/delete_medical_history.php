<?php
header("Content-Type: application/json");
require 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    $historyId = $data['history_id'] ?? null;
    $staffId = $data['staff_id'] ?? null;
    
    if (!$historyId || !$staffId) {
        echo json_encode(["success" => false, "message" => "History ID and Staff ID are required"]);
        exit;
    }
    
    try {
        // Verify the history belongs to the staff before deleting
        $stmt = $conn->prepare("DELETE FROM medical_history WHERE id = ? AND created_by = ?");
        $stmt->bind_param("ii", $historyId, $staffId);
        
        if ($stmt->execute()) {
            if ($stmt->affected_rows > 0) {
                echo json_encode(["success" => true, "message" => "Medical history deleted successfully"]);
            } else {
                echo json_encode(["success" => false, "message" => "Medical history not found or you don't have permission to delete it"]);
            }
        } else {
            throw new Exception("Failed to delete medical history: " . $stmt->error);
        }
        
        $stmt->close();
    } catch (Exception $e) {
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
    
    $conn->close();
}
?>