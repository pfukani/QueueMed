package com.queuemed.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.queuemed.R;
import com.queuemed.api.ApiClient;
import com.queuemed.api.ApiService;
import com.queuemed.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffMedicalHistoryActivity extends AppCompatActivity {

    private TextInputEditText etPatientDetails, etDiagnosedConditions, etHereditaryDiseases,
            etAllergies, etVaccinationStatus, etSexualHistory, etNotes;
    private Button btnEdit, btnSave, btnDelete, btnCancel;
    private ImageButton btnBack;
    private LinearLayout layoutDelete;
    private SharedPrefManager sp;
    private int staffId;
    private String mode = "add"; // "add", "view", "edit"
    private int historyId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        sp = new SharedPrefManager(this);
        staffId = sp.getUserId();

        initializeViews();
        setupClickListeners();

        // Check if we're viewing/editing existing history
        if (getIntent().hasExtra("history_id")) {
            historyId = getIntent().getIntExtra("history_id", -1);
            mode = getIntent().getStringExtra("mode");
            if ("view".equals(mode)) {
                setFieldsEditable(false);
                btnEdit.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                layoutDelete.setVisibility(View.GONE);
                loadMedicalHistory(historyId);
            } else if ("edit".equals(mode)) {
                setFieldsEditable(true);
                btnEdit.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
                layoutDelete.setVisibility(View.VISIBLE);
                loadMedicalHistory(historyId);
            }
        } else {
            setFieldsEditable(true);
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            layoutDelete.setVisibility(View.GONE);
        }
    }

    private void initializeViews() {
        etPatientDetails = findViewById(R.id.etPatientDetails);
        etDiagnosedConditions = findViewById(R.id.etDiagnosedConditions);
        etHereditaryDiseases = findViewById(R.id.etHereditaryDiseases);
        etAllergies = findViewById(R.id.etAllergies);
        etVaccinationStatus = findViewById(R.id.etVaccinationStatus);
        etSexualHistory = findViewById(R.id.etSexualHistory);
        etNotes = findViewById(R.id.etNotes);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Delete functionality views
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);
        layoutDelete = findViewById(R.id.layoutDelete);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            mode = "edit";
            setFieldsEditable(true);
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            layoutDelete.setVisibility(View.VISIBLE); // Show delete option in edit mode
        });

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                if (mode.equals("add")) {
                    saveMedicalHistory();
                } else if (mode.equals("edit")) {
                    updateMedicalHistory();
                }
            }
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        btnCancel.setOnClickListener(v -> {
            layoutDelete.setVisibility(View.GONE);
            if (mode.equals("view")) {
                setFieldsEditable(false);
                btnEdit.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
            }
        });
    }

    private boolean validateInputs() {
        if (etPatientDetails.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Patient details are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setFieldsEditable(boolean enabled) {
        etPatientDetails.setEnabled(enabled);
        etDiagnosedConditions.setEnabled(enabled);
        etHereditaryDiseases.setEnabled(enabled);
        etAllergies.setEnabled(enabled);
        etVaccinationStatus.setEnabled(enabled);
        etSexualHistory.setEnabled(enabled);
        etNotes.setEnabled(enabled);
    }

    private void loadMedicalHistory(int historyId) {
        // Show loading
        Toast.makeText(this, "Loading medical history...", Toast.LENGTH_SHORT).show();

        // Make API call
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<Map<String, Object>> call = api.getMedicalHistory(historyId);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    if (success) {
                        Map<String, Object> history = (Map<String, Object>) response.body().get("data");
                        if (history != null) {
                            // Populate the fields with data from database
                            etPatientDetails.setText(history.get("patient_details").toString());
                            etDiagnosedConditions.setText(getSafeString(history, "diagnosed_conditions"));
                            etHereditaryDiseases.setText(getSafeString(history, "hereditary_diseases"));
                            etAllergies.setText(getSafeString(history, "allergies"));
                            etVaccinationStatus.setText(getSafeString(history, "vaccination_status"));
                            etSexualHistory.setText(getSafeString(history, "sexual_history"));
                            etNotes.setText(getSafeString(history, "notes"));

                            Toast.makeText(StaffMedicalHistoryActivity.this, "Medical history loaded", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String message = response.body().get("message").toString();
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StaffMedicalHistoryActivity.this, "Failed to load medical history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(StaffMedicalHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to safely get string values
    private String getSafeString(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            return map.get(key).toString();
        }
        return "";
    }

    private void saveMedicalHistory() {
        String patientDetails = etPatientDetails.getText().toString().trim();
        String diagnosedConditions = etDiagnosedConditions.getText().toString().trim();
        String hereditaryDiseases = etHereditaryDiseases.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String vaccinationStatus = etVaccinationStatus.getText().toString().trim();
        String sexualHistory = etSexualHistory.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Show loading
        Toast.makeText(this, "Saving medical history...", Toast.LENGTH_SHORT).show();

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("patient_details", patientDetails);
        requestBody.put("diagnosed_conditions", diagnosedConditions);
        requestBody.put("hereditary_diseases", hereditaryDiseases);
        requestBody.put("allergies", allergies);
        requestBody.put("vaccination_status", vaccinationStatus);
        requestBody.put("sexual_history", sexualHistory);
        requestBody.put("notes", notes);
        requestBody.put("created_by", staffId);

        // Make API call
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<Map<String, Object>> call = api.saveMedicalHistory(requestBody);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    if (success) {
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Medical history saved successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity and return to fragment
                    } else {
                        String message = response.body().get("message").toString();
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StaffMedicalHistoryActivity.this, "Failed to save medical history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(StaffMedicalHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateMedicalHistory() {
        String patientDetails = etPatientDetails.getText().toString().trim();
        String diagnosedConditions = etDiagnosedConditions.getText().toString().trim();
        String hereditaryDiseases = etHereditaryDiseases.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String vaccinationStatus = etVaccinationStatus.getText().toString().trim();
        String sexualHistory = etSexualHistory.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Show loading
        Toast.makeText(this, "Updating medical history...", Toast.LENGTH_SHORT).show();

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("history_id", historyId);
        requestBody.put("patient_details", patientDetails);
        requestBody.put("diagnosed_conditions", diagnosedConditions);
        requestBody.put("hereditary_diseases", hereditaryDiseases);
        requestBody.put("allergies", allergies);
        requestBody.put("vaccination_status", vaccinationStatus);
        requestBody.put("sexual_history", sexualHistory);
        requestBody.put("notes", notes);
        requestBody.put("staff_id", staffId); // Use staff_id instead of created_by for update

        // Make API call using the new update endpoint
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<Map<String, Object>> call = api.updateMedicalHistory(requestBody);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    if (success) {
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Medical history updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String message = response.body().get("message").toString();
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StaffMedicalHistoryActivity.this, "Failed to update medical history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(StaffMedicalHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medical History")
                .setMessage("Are you sure you want to delete this medical history? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteMedicalHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMedicalHistory() {
        // Show loading
        Toast.makeText(this, "Deleting medical history...", Toast.LENGTH_SHORT).show();

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("history_id", historyId);
        requestBody.put("staff_id", staffId);

        // Make API call
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<Map<String, Object>> call = api.deleteMedicalHistory(requestBody);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    if (success) {
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Medical history deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity and return to fragment
                    } else {
                        String message = response.body().get("message").toString();
                        Toast.makeText(StaffMedicalHistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StaffMedicalHistoryActivity.this, "Failed to delete medical history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(StaffMedicalHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}