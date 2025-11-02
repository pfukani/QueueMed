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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DailySummaryAdapter adapter;
    private List<DailySummary> summaryList = new ArrayList<>();

    private DatabaseReference appointmentsRef;

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

        loadDailyReport();
        setupExportButton(view);


        return view;
    }

    private void loadDailyReport() {
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, DailySummary> dailyMap = new HashMap<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Appointment appt = ds.getValue(Appointment.class);
                    if (appt == null) continue;

                    String dateKey = appt.getDate(); // already stored as string

                    DailySummary summary = dailyMap.getOrDefault(dateKey, new DailySummary(dateKey));
                    summary.incrementAppointments();

                    if ("checked_in".equalsIgnoreCase(appt.getStatus())) {
                        summary.incrementCheckedIn();
                    }
                    if ("vitals_taken".equalsIgnoreCase(appt.getStatus())) {
                        summary.incrementVitalsTaken();
                    }

                    dailyMap.put(dateKey, summary);
                }

                summaryList.clear();
                summaryList.addAll(dailyMap.values());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("ReportsFragment", "Firebase error: " + error.getMessage());
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