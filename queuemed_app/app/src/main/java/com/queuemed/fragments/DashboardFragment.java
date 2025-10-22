package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.queuemed.adapters.AppointmentsAdapter;
import com.queuemed.adapters.AppointmentsAdapter.CheckInCallback;
import com.queuemed.models.Appointment;
import com.queuemed.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvWelcome, tvUpcomingAppointments, tvCheckedIn;
    private RecyclerView recyclerRecentAppointments;
    private AppointmentsAdapter adapter;
    private List<Appointment> recentAppointments = new ArrayList<>();

    private DatabaseReference dbRef;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvUpcomingAppointments = view.findViewById(R.id.tvUpcomingAppointments);
        tvCheckedIn = view.findViewById(R.id.tvCheckedIn);
        recyclerRecentAppointments = view.findViewById(R.id.recyclerRecentAppointments);

        sp = new SharedPrefManager(getContext());
        String userName = sp.getUserName();
        tvWelcome.setText("Welcome, " + userName);

        // Updated adapter with CheckInCallback
        adapter = new AppointmentsAdapter(getContext(), recentAppointments, new CheckInCallback() {
            @Override
            public void onCheckIn(Appointment appointment) {
                // Optional: Handle check-in click if needed on Dashboard
                // For now, do nothing or show a Toast
            }
        });

        recyclerRecentAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecentAppointments.setAdapter(adapter);

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        String userEmailKey = sp.getUserEmail().replace(".", "_");
        dbRef = FirebaseDatabase.getInstance().getReference("appointments").child(userEmailKey);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentAppointments.clear();
                int upcomingCount = 0, checkedInCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Appointment a = ds.getValue(Appointment.class);
                    if (a != null) {
                        recentAppointments.add(a);
                        if ("Pending".equals(a.getStatus())) upcomingCount++;
                        if ("CheckedIn".equals(a.getStatus())) checkedInCount++;
                    }
                }

                tvUpcomingAppointments.setText(String.valueOf(upcomingCount));
                tvCheckedIn.setText(String.valueOf(checkedInCount));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
