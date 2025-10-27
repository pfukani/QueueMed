package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.models.PatientRecord;
import com.queuemed.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalHistoryFragment extends Fragment {

    private TextView tvHistory;
    private DatabaseReference dbRecordsRef;
    private SharedPrefManager sp;
    private List<PatientRecord> historyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_history, container, false);
        tvHistory = view.findViewById(R.id.tvMedicalHistory);

        sp = new SharedPrefManager(requireContext());
        String userEmailKey = sp.getUserEmail().replace(".", "_");

        dbRecordsRef = FirebaseDatabase.getInstance()
                .getReference("patient_records")
                .child(userEmailKey);

        dbRecordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                StringBuilder historyText = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PatientRecord record = ds.getValue(PatientRecord.class);
                    if (record != null) {
                        historyList.add(record);
                        String date = sdf.format(new Date(record.getTimestamp()));
                        historyText.append(" ").append(date).append("\n")
                                .append("BP: ").append(record.getBloodPressure()).append("\n")
                                .append("Temp: ").append(record.getTemperature()).append("°C\n")
                                .append("Pulse: ").append(record.getPulse()).append(" bpm\n")
                                .append("Notes: ").append(record.getNotes()).append("\n")
                                .append("Status: ").append(record.getStatus()).append("\n\n");
                    }
                }

                if (historyText.length() == 0) {
                    historyText.append("No medical history found.");
                }

                tvHistory.setText(historyText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return view;
    }
}
