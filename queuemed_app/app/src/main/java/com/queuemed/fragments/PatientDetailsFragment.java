package com.queuemed.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.models.Appointment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PatientDetailsFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_EMAIL = "email";

    private String patientName;
    private String patientEmail;

    private TextView tvName, tvEmail;
    private EditText etBP, etTemp, etPulse, etNotes;
    private Button btnSaveVitals, btnMarkCompleted, btnViewHistory;

    private DatabaseReference dbRecordsRef, dbQueueRef;

    public static PatientDetailsFragment newInstance(String name, String email) {
        PatientDetailsFragment fragment = new PatientDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientName = getArguments().getString(ARG_NAME);
            patientEmail = getArguments().getString(ARG_EMAIL);
        }

        dbRecordsRef = FirebaseDatabase.getInstance().getReference("patient_records");
        dbQueueRef = FirebaseDatabase.getInstance().getReference("queue");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_details, container, false);

        tvName = view.findViewById(R.id.tvPatientName);
        tvEmail = view.findViewById(R.id.tvPatientEmail);
        etBP = view.findViewById(R.id.etBloodPressure);
        etTemp = view.findViewById(R.id.etTemperature);
        etPulse = view.findViewById(R.id.etPulse);
        etNotes = view.findViewById(R.id.etNotes);
        btnSaveVitals = view.findViewById(R.id.btnSaveVitals);
        btnMarkCompleted = view.findViewById(R.id.btnMarkCompleted);
        btnViewHistory = view.findViewById(R.id.btnViewHistory);

        tvName.setText(patientName);
        tvEmail.setText(patientEmail);

        btnSaveVitals.setOnClickListener(v -> saveVitals());
        btnMarkCompleted.setOnClickListener(v -> markVisitCompleted());
        btnViewHistory.setOnClickListener(v -> showHistory());

        return view;
    }

    private void saveVitals() {
        String bp = etBP.getText().toString().trim();
        String temp = etTemp.getText().toString().trim();
        String pulse = etPulse.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(bp) || TextUtils.isEmpty(temp) || TextUtils.isEmpty(pulse)) {
            Toast.makeText(getContext(), "Please fill in all vitals", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailKey = patientEmail.replace(".", "_");
        String visitId = UUID.randomUUID().toString();

        Map<String, Object> vitalsData = new HashMap<>();
        vitalsData.put("bloodPressure", bp);
        vitalsData.put("temperature", temp);
        vitalsData.put("pulse", pulse);
        vitalsData.put("notes", notes);
        vitalsData.put("timestamp", System.currentTimeMillis());
        vitalsData.put("status", "Ongoing");
        vitalsData.put("patientName", patientName); // Add patient name for reporting

        dbRecordsRef.child(emailKey).child(visitId).setValue(vitalsData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Vitals saved successfully", Toast.LENGTH_SHORT).show();

                    // Update reports count - increment vitals taken
                    updateVitalsTakenCount();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save vitals", Toast.LENGTH_SHORT).show());
    }

    private void updateVitalsTakenCount() {
        String today = java.time.LocalDate.now().toString();
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference("daily_reports").child(today);

        reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentVitals;
                if (snapshot.exists() && snapshot.child("vitalsTaken").getValue() != null) {
                    currentVitals = snapshot.child("vitalsTaken").getValue(Integer.class);
                } else {
                    currentVitals = 0;
                }
                // Ensure we're setting an integer value
                reportsRef.child("vitalsTaken").setValue(currentVitals + 1)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("VitalsCount", "Vitals count updated to: " + (currentVitals + 1));
                        })
                        .addOnFailureListener(e -> {
                            Log.e("VitalsCount", "Failed to update vitals count: " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("VitalsCount", "Failed to read vitals count: " + error.getMessage());
            }
        });
    }

    private void markVisitCompleted() {
        String emailKey = patientEmail.replace(".", "_");

        // Update queue status to "Completed"
        dbQueueRef.orderByChild("patientEmail").equalTo(patientEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().child("status").setValue("Completed");
                        }

                        // Update appointment status to "Completed"
                        updateAppointmentStatus();

                        // Update patient records status from "Ongoing" to "Completed"
                        updatePatientRecordsStatus(emailKey);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to update queue", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAppointmentStatus() {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        String emailKey = patientEmail.replace(".", "_");

        appointmentsRef.child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnap : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnap.getValue(Appointment.class);
                    if (appointment != null && "Checked In".equalsIgnoreCase(appointment.getStatus())) {
                        appointmentSnap.getRef().child("status").setValue("Completed");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to update appointment status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updatePatientRecordsStatus(String emailKey) {
        dbRecordsRef.child(emailKey)
                .orderByChild("status").equalTo("Ongoing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot record : snapshot.getChildren()) {
                            record.getRef().child("status").setValue("Completed");
                        }

                        Toast.makeText(getContext(), "Visit marked as completed", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to update records", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showHistory() {
        String emailKey = patientEmail.replace(".", "_");

        dbRecordsRef.child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "No history found", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder historyText = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot visit : snapshot.getChildren()) {
                    long timestamp = visit.child("timestamp").getValue(Long.class) != null
                            ? visit.child("timestamp").getValue(Long.class) : 0L;
                    String bp = visit.child("bloodPressure").getValue(String.class);
                    String temp = visit.child("temperature").getValue(String.class);
                    String pulse = visit.child("pulse").getValue(String.class);
                    String notes = visit.child("notes").getValue(String.class);
                    String status = visit.child("status").getValue(String.class);

                    String date = timestamp == 0 ? "Unknown Date" : sdf.format(new Date(timestamp));

                    historyText.append("📅 ").append(date).append("\n")
                            .append(" BP: ").append(bp != null ? bp : "-").append("\n")
                            .append("🌡 Temp: ").append(temp != null ? temp : "-").append("°C\n")
                            .append(" Pulse: ").append(pulse != null ? pulse : "-").append(" bpm\n")
                            .append(" Notes: ").append(notes != null ? notes : "-").append("\n")
                            .append(" Status: ").append(status != null ? status : "N/A")
                            .append("\n\n");
                }

                // Create a scrollable layout
                ScrollView scrollView = new ScrollView(getContext());
                scrollView.setBackgroundColor(0xFF000000); // black background

                TextView textView = new TextView(getContext());
                textView.setText(historyText.toString());
                textView.setPadding(40, 30, 40, 30);
                textView.setTextColor(0xFFFFFDD0); // cream text
                textView.setTextSize(16);
                scrollView.addView(textView);

                // Build the dialog with dark theme
                AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.DarkDialogTheme)
                        .setTitle("Patient History")
                        .setView(scrollView)
                        .setPositiveButton("CLOSE", null)
                        .create();

                dialog.show();

                // Customize title and button text color
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFFFFDD0);
                int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                TextView titleView = dialog.findViewById(titleId);
                if (titleView != null) titleView.setTextColor(0xFFFFFDD0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}