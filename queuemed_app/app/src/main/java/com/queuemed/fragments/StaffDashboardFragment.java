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

public class StaffDashboardFragment extends Fragment {

    private TextView tvQueueCount, tvAppointmentsCount;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_dashboard, container, false);

        tvQueueCount = view.findViewById(R.id.tvQueueCount);
        tvAppointmentsCount = view.findViewById(R.id.tvAppointmentsCount);

        dbRef = FirebaseDatabase.getInstance().getReference("appointments");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAppointments = 0;
                for(DataSnapshot userNode : snapshot.getChildren()){
                    totalAppointments += userNode.getChildrenCount();
                }
                tvAppointmentsCount.setText("Total Appointments: " + totalAppointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // TODO: You can also count patients in queue if you have a queue node

        return view;
    }
}
