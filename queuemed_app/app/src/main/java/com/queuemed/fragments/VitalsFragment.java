package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.shimmer.ShimmerFrameLayout;
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
    private LinearLayout contentLayout;
    private ShimmerFrameLayout shimmerLayout;
    private DatabaseReference ref;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vitals, container, false);

        // Initialize Views
        tvBP = view.findViewById(R.id.tvBloodPressure);
        tvTemp = view.findViewById(R.id.tvTemperature);
        tvPulse = view.findViewById(R.id.tvPulse);
        tvTimestamp = view.findViewById(R.id.tvTimestamp);
        contentLayout = view.findViewById(R.id.contentLayout);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);

        sp = new SharedPrefManager(requireContext());

        // Optional: Pulse/Heart animation icons
        ImageView ivPulse = view.findViewById(R.id.ivPulseIcon);
        ImageView ivHeart = view.findViewById(R.id.ivHeartIcon);
        ImageView ivTemp = view.findViewById(R.id.ivTempIcon);

        loadPatientVitals();

        return view;
    }

    private void loadPatientVitals() {
        String userEmail = sp.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailKey = userEmail.replace(".", "_");
        ref = FirebaseDatabase.getInstance().getReference("patient_records").child(emailKey);

        // Show shimmer
        shimmerLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        ref.orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        shimmerLayout.setVisibility(View.GONE);
                        contentLayout.setVisibility(View.VISIBLE);

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
                            String bp = getValue(latestRecord, "bloodPressure", "bp", "blood_pressure");
                            String temp = getValue(latestRecord, "temperature", "temp");
                            String pulse = getValue(latestRecord, "pulse", "heartRate");
                            Long timestamp = latestRecord.child("timestamp").getValue(Long.class);

                            tvBP.setText(bp != null ? bp + " mmHg" : "--/-- mmHg");
                            tvTemp.setText(temp != null ? temp + " °C" : "-- °C");
                            tvPulse.setText(pulse != null ? pulse + " bpm" : "-- bpm");

                            // BP color coding
                            if (bp != null && bp.contains("/")) {
                                try {
                                    String[] parts = bp.split("/");
                                    int sys = Integer.parseInt(parts[0]);
                                    int dia = Integer.parseInt(parts[1]);
                                    if (sys > 140 || dia > 90)
                                        tvBP.setTextColor(0xFFFF5555); // high -> red
                                    else
                                        tvBP.setTextColor(0xFF00FF00); // normal -> green
                                } catch (NumberFormatException ignored) {}
                            }

                            // Temperature color coding
                            if (temp != null) {
                                try {
                                    float t = Float.parseFloat(temp);
                                    if (t > 37.5)
                                        tvTemp.setTextColor(0xFFFF5555); // fever -> red
                                    else
                                        tvTemp.setTextColor(0xFF00FF00); // normal -> green
                                } catch (NumberFormatException ignored) {}
                            }

                            // Pulse color coding
                            if (pulse != null) {
                                try {
                                    int p = Integer.parseInt(pulse);
                                    if (p < 60 || p > 100)
                                        tvPulse.setTextColor(0xFFFF5555); // abnormal -> red
                                    else
                                        tvPulse.setTextColor(0xFF00FF00); // normal -> green
                                } catch (NumberFormatException ignored) {}
                            }

                            // Timestamp
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
                        shimmerLayout.setVisibility(View.GONE);
                        contentLayout.setVisibility(View.VISIBLE);
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
