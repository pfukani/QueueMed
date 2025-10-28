package com.queuemed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.queuemed.R;
import com.queuemed.activities.StaffMedicalHistoryActivity;
import com.queuemed.api.ApiClient;
import com.queuemed.api.ApiService;
import com.queuemed.utils.SharedPrefManager;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffMedicalHistoryFragment extends Fragment {

    private LinearLayout containerPatientHistory;
    private MaterialButton btnAddHistory;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_history_staff, container, false);

        sp = new SharedPrefManager(getContext());
        containerPatientHistory = view.findViewById(R.id.containerPatientHistory);
        btnAddHistory = view.findViewById(R.id.btnAddHistory);


        btnAddHistory.setOnClickListener(v -> {
            // Open activity to add new medical history
            Intent intent = new Intent(getActivity(), StaffMedicalHistoryActivity.class);
            startActivity(intent);
        });

        loadMedicalHistories();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list when returning from adding/editing
        loadMedicalHistories();
    }

    private void loadMedicalHistories() {
        // Clear existing views
        containerPatientHistory.removeAllViews();

        // Show loading
        TextView loading = new TextView(getContext());
        loading.setText("Loading medical histories...");
        loading.setTextColor(0xFFCCCCCC);
        loading.setTextSize(16f);
        containerPatientHistory.addView(loading);

        // Make API call
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<Map<String, Object>> call = api.getMedicalHistories(sp.getUserId());

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                containerPatientHistory.removeAllViews();

                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    if (success) {
                        List<Map<String, Object>> histories = (List<Map<String, Object>>) response.body().get("data");
                        if (histories != null && !histories.isEmpty()) {
                            for (Map<String, Object> history : histories) {
                                String patientDetails = history.get("patient_details").toString();

                                // FIX: Handle the ID as double/float
                                int historyId = -1;
                                if (history.containsKey("id")) {
                                    try {
                                        double idDouble = Double.parseDouble(history.get("id").toString());
                                        historyId = (int) idDouble;
                                    } catch (NumberFormatException e) {
                                        Log.e("MEDICAL_HISTORY", "Failed to parse history ID: " + history.get("id"));
                                    }
                                }

                                addPatientHistoryTab(patientDetails, historyId);
                            }
                        } else {
                            showNoHistoryMessage();
                        }
                    } else {
                        showNoHistoryMessage();
                    }
                } else {
                    showNoHistoryMessage();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                containerPatientHistory.removeAllViews();
                showNoHistoryMessage();
                Toast.makeText(getContext(), "Failed to load histories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoHistoryMessage() {
        TextView noHistory = new TextView(getContext());
        noHistory.setText("No medical histories available");
        noHistory.setTextColor(0xFFCCCCCC);
        noHistory.setTextSize(16f);
        containerPatientHistory.addView(noHistory);
    }

    private void addPatientHistoryTab(String patientDetails, int historyId) {
        MaterialButton patientTab = new MaterialButton(getContext());
        patientTab.setText(patientDetails);
        patientTab.setBackgroundColor(0xFF1E1E1E);
        patientTab.setTextColor(0xFFFFFFFF);
        patientTab.setCornerRadius(16);
        patientTab.setStrokeColorResource(android.R.color.darker_gray);
        patientTab.setStrokeWidth(2);
        patientTab.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) patientTab.getLayoutParams();
        params.setMargins(0, 0, 0, 16);
        patientTab.setLayoutParams(params);

        patientTab.setOnClickListener(v -> {
            // Open view activity for this specific medical history
            Intent intent = new Intent(getActivity(), StaffMedicalHistoryActivity.class);
            intent.putExtra("history_id", historyId);
            intent.putExtra("mode", "view");
            startActivity(intent);
        });

        containerPatientHistory.addView(patientTab);
    }
}