package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.queuemed.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VitalsFragment extends Fragment {

    private TextView tvBP, tvTemp, tvPulse, tvTimestamp;
    private DatabaseReference ref;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vitals, container, false);

        // Initialize UI
        tvBP = view.findViewById(R.id.tvBloodPressure);
        tvTemp = view.findViewById(R.id.tvTemperature);
        tvPulse = view.findViewById(R.id.tvPulse);
        tvTimestamp = view.findViewById(R.id.tvTimestamp);

        // Initialize SharedPref
        sp = new SharedPrefManager(requireContext());

        loadPatientVitals();

        return view;
    }

    private void loadPatientVitals() {
        String userEmail = sp.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase key-safe email
        String emailKey = userEmail.replace(".", "_");
        ref = FirebaseDatabase.getInstance().getReference("patient_records").child(emailKey);

        // Retrieve the most recent vitals entry
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    tvBP.setText("--/-- mmHg");
                    tvTemp.setText("-- °C");
                    tvPulse.setText("-- bpm");
                    tvTimestamp.setText("Last Updated: --");
                    return;
                }

                DataSnapshot latestRecord = null;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    latestRecord = ds;
                }

                if (latestRecord != null) {
                    // Try multiple possible key variations (to handle old DB records)
                    String bp = getValue(latestRecord, "bloodPressure", "bp", "blood_pressure");
                    String temp = getValue(latestRecord, "temperature", "temp");
                    String pulse = getValue(latestRecord, "pulse", "heartRate");
                    Long timestamp = latestRecord.child("timestamp").getValue(Long.class);

                    tvBP.setText(bp != null ? bp + " mmHg" : "--/-- mmHg");
                    tvTemp.setText(temp != null ? temp + " °C" : "-- °C");
                    tvPulse.setText(pulse != null ? pulse + " bpm" : "-- bpm");

                    if (timestamp != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
                        tvTimestamp.setText("Last Updated: " + sdf.format(new Date(timestamp)));
                    } else {
                        tvTimestamp.setText("Last Updated: --");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load vitals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getValue(DataSnapshot snapshot, String... keys) {
        for (String key : keys) {
            if (snapshot.child(key).exists()) {
                return snapshot.child(key).getValue(String.class);
            }
        }
        return null;
    }
}
