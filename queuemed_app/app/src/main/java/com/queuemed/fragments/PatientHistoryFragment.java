package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.queuemed.R;
import com.queuemed.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PatientHistoryFragment extends Fragment {

    private LinearLayout historyContainer;
    private DatabaseReference dbRecordsRef;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_history, container, false);

        historyContainer = view.findViewById(R.id.historyContainer);
        sp = new SharedPrefManager(requireContext());

        loadMedicalHistory();

        return view;
    }

    private void loadMedicalHistory() {
        String emailKey = sp.getUserEmail().replace(".", "_");
        dbRecordsRef = FirebaseDatabase.getInstance().getReference("patient_records").child(emailKey);

        dbRecordsRef.orderByChild("timestamp")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyContainer.removeAllViews();

                        if (!snapshot.exists()) {
                            Toast.makeText(getContext(), "No medical history found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

                        for (DataSnapshot visit : snapshot.getChildren()) {
                            Long timestamp = visit.child("timestamp").getValue(Long.class);
                            String bp = visit.child("bloodPressure").getValue(String.class);
                            String temp = visit.child("temperature").getValue(String.class);
                            String pulse = visit.child("pulse").getValue(String.class);
                            String notes = visit.child("notes").getValue(String.class);
                            String status = visit.child("status").getValue(String.class);

                            // Skip records with no timestamp or "Unknown Date"
                            if (timestamp == null || timestamp == 0) {
                                continue;
                            }

                            // Create card only for valid records
                            createHistoryCard(sdf, timestamp, bp, temp, pulse, notes, status);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createHistoryCard(SimpleDateFormat sdf, Long timestamp, String bp, String temp,
                                   String pulse, String notes, String status) {
        // Card container
        CardView card = new CardView(requireContext());
        card.setCardElevation(6);
        card.setRadius(12);
        card.setUseCompatPadding(true);
        card.setContentPadding(20, 20, 20, 20);
        card.setCardBackgroundColor(0xFF2C2C2C);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Date - Now guaranteed to be valid
        TextView tvDate = new TextView(requireContext());
        tvDate.setText("📅 " + sdf.format(new Date(timestamp)));
        tvDate.setTextColor(0xFFFFFFFF);
        tvDate.setTextSize(16);
        tvDate.setPadding(0, 0, 0, 8);

        // Vitals
        TextView tvVitals = new TextView(requireContext());
        tvVitals.setText(
                "BP: " + (bp != null ? bp + " mmHg" : "--/--") + "\n" +
                        "Temp: " + (temp != null ? temp + "°C" : "--") + "\n" +
                        "Pulse: " + (pulse != null ? pulse + " bpm" : "--")
        );
        tvVitals.setTextColor(0xFFDDDDDD);
        tvVitals.setPadding(0, 0, 0, 8);

        // Notes
        TextView tvNotes = new TextView(requireContext());
        tvNotes.setText("Notes: " + (notes != null ? notes : "-"));
        tvNotes.setTextColor(0xFFCCCCCC);
        tvNotes.setPadding(0, 0, 0, 8);

        // Status
        TextView tvStatus = new TextView(requireContext());
        tvStatus.setText("Status: " + (status != null ? status : "N/A"));
        tvStatus.setTextColor(
                status != null && status.equalsIgnoreCase("Completed")
                        ? 0xFF00FF00 // green
                        : 0xFFFFFF00 // yellow for Ongoing
        );

        layout.addView(tvDate);
        layout.addView(tvVitals);
        layout.addView(tvNotes);
        layout.addView(tvStatus);

        card.addView(layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        historyContainer.addView(card);
    }
}
