package com.queuemed.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.adapters.DailySummaryAdapter;
import com.queuemed.models.Appointment;
import com.queuemed.models.DailySummary;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DailySummaryAdapter adapter;
    private List<DailySummary> summaryList = new ArrayList<>();

    private DatabaseReference appointmentsRef, queueRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_report, container, false);

        recyclerView = view.findViewById(R.id.recyclerReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new DailySummaryAdapter(summaryList);
        recyclerView.setAdapter(adapter);

        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        queueRef = FirebaseDatabase.getInstance().getReference("queue");

        loadDailyReport();
        setupExportButton(view);

        return view;
    }

    private void loadDailyReport() {
        // Step 1: Load appointments
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, DailySummary> dailyMap = new HashMap<>();

                for (DataSnapshot userNode : snapshot.getChildren()) {
                    for (DataSnapshot apptNode : userNode.getChildren()) {
                        Object raw = apptNode.getValue();
                        if (!(raw instanceof Map)) continue;

                        Appointment appt = apptNode.getValue(Appointment.class);
                        if (appt == null || appt.getDate() == null) continue;

                        String dateKey = appt.getDate();
                        DailySummary summary = dailyMap.getOrDefault(dateKey, new DailySummary(dateKey));

                        summary.incrementAppointments();

                        if ("Checked In".equalsIgnoreCase(appt.getStatus())) {
                            summary.incrementCheckedIn();
                        }

                        dailyMap.put(dateKey, summary);
                    }
                }

                // Step 2: Load vitals from patient_records (NEW IMPROVED METHOD)
                loadVitalsForReports(dailyMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVitalsForReports(Map<String, DailySummary> dailyMap) {
        DatabaseReference recordsRef = FirebaseDatabase.getInstance().getReference("patient_records");
        DatabaseReference dailyReportsRef = FirebaseDatabase.getInstance().getReference("daily_reports");

        // First, check if we have data in daily_reports
        dailyReportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Use data from daily_reports which is updated when vitals are saved
                    for (DataSnapshot dateSnap : snapshot.getChildren()) {
                        String dateKey = dateSnap.getKey();
                        Integer vitalsTaken = dateSnap.child("vitalsTaken").getValue(Integer.class);

                        if (dateKey != null && vitalsTaken != null) {
                            DailySummary summary = dailyMap.getOrDefault(dateKey, new DailySummary(dateKey));
                            summary.setVitalsTaken(vitalsTaken);
                            dailyMap.put(dateKey, summary);
                        }
                    }

                    // Update UI
                    summaryList.clear();
                    summaryList.addAll(dailyMap.values());
                    adapter.notifyDataSetChanged();
                } else {
                    // Fallback: Count from patient_records
                    countVitalsFromRecords(dailyMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                countVitalsFromRecords(dailyMap);
            }
        });
    }

    private void countVitalsFromRecords(Map<String, DailySummary> dailyMap) {
        DatabaseReference recordsRef = FirebaseDatabase.getInstance().getReference("patient_records");

        recordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot patientNode : snapshot.getChildren()) {
                    for (DataSnapshot record : patientNode.getChildren()) {
                        Long timestamp = record.child("timestamp").getValue(Long.class);
                        // Count ALL vitals records, not just "Ongoing"
                        if (timestamp != null) {
                            LocalDate date = Instant.ofEpochMilli(timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            String dateKey = date.toString();

                            DailySummary summary = dailyMap.getOrDefault(dateKey, new DailySummary(dateKey));
                            summary.incrementVitalsTaken();
                            dailyMap.put(dateKey, summary);
                        }
                    }
                }

                // Update UI
                summaryList.clear();
                summaryList.addAll(dailyMap.values());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load vitals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExportButton(View view) {
        Button btnExportCsv = view.findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> exportToCsv());
    }

    private void exportToCsv() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "daily_reports.csv");
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try (OutputStream out = getContext().getContentResolver().openOutputStream(uri);
                 OutputStreamWriter writer = new OutputStreamWriter(out)) {

                writer.write("Date,Total Appointments,Checked In,Vitals Taken\n");
                for (DailySummary summary : summaryList) {
                    writer.write(summary.getDate() + "," +
                            summary.getTotalAppointments() + "," +
                            summary.getCheckedIn() + "," +
                            summary.getVitalsTaken() + "\n");
                }
                writer.flush();
                Toast.makeText(getContext(), "Exported successfully", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}