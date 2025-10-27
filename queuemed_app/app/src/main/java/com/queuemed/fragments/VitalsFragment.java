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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.utils.SharedPrefManager;

import java.util.Locale;

public class VitalsFragment extends Fragment {

    private TextView tvBP, tvTemp, tvPulse, tvTimestamp;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vitals, container, false);

        sp = new SharedPrefManager(requireContext()); // initialize SharedPrefManager

        tvBP = view.findViewById(R.id.tvBP);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvPulse = view.findViewById(R.id.tvPulse);
        tvTimestamp = view.findViewById(R.id.tvVitalsTimestamp);

        loadVitals();

        return view;
    }

    private void loadVitals() {
        String emailKey = sp.getUserEmail() != null ? sp.getUserEmail().replace(".", "_") : "";

        FirebaseDatabase.getInstance().getReference("patient_records")
                .child(emailKey)
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String bp = ds.child("bloodPressure").getValue(String.class);
                            String temp = ds.child("temperature").getValue(String.class);
                            String pulse = ds.child("pulse").getValue(String.class);
                            Long timestamp = ds.child("timestamp").getValue(Long.class);

                            tvBP.setText(bp != null ? bp + " mmHg" : "--/-- mmHg");
                            tvTemp.setText(temp != null ? temp + " °C" : "-- °C");
                            tvPulse.setText(pulse != null ? pulse + " bpm" : "-- bpm");

                            // Color coding based on normal ranges
                            if (bp != null) {
                                String[] parts = bp.split("/");
                                if (parts.length == 2) {
                                    try {
                                        int sys = Integer.parseInt(parts[0]);
                                        int dia = Integer.parseInt(parts[1]);
                                        if (sys > 140 || dia > 90) tvBP.setTextColor(0xFFFF5555); // high BP -> red
                                        else tvBP.setTextColor(0xFF00FF00); // normal -> green
                                    } catch (NumberFormatException ignored) {}
                                }
                            }

                            if (temp != null) {
                                try {
                                    float t = Float.parseFloat(temp);
                                    if (t > 37.5) tvTemp.setTextColor(0xFFFF5555); // fever -> red
                                    else tvTemp.setTextColor(0xFF00FF00); // normal -> green
                                } catch (NumberFormatException ignored) {}
                            }

                            if (pulse != null) {
                                try {
                                    int p = Integer.parseInt(pulse);
                                    if (p < 60 || p > 100) tvPulse.setTextColor(0xFFFF5555); // abnormal -> red
                                    else tvPulse.setTextColor(0xFF00FF00); // normal -> green
                                } catch (NumberFormatException ignored) {}
                            }

                            if (timestamp != null) {
                                java.text.SimpleDateFormat sdf =
                                        new java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault());
                                tvTimestamp.setText("Last Updated: " + sdf.format(new java.util.Date(timestamp)));
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

}
