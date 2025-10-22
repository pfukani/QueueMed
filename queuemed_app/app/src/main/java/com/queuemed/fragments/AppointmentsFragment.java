package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.queuemed.models.Appointment;
import com.queuemed.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointmentList;
    private DatabaseReference dbRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        appointmentList = new ArrayList<>();

        // Adapter with callback
        adapter = new AppointmentsAdapter(getContext(), appointmentList, new AppointmentsAdapter.CheckInCallback() {
            @Override
            public void onCheckIn(Appointment appointment) {
                // Update status in Firebase
                if (appointment.getId() != null) {
                    dbRef.child(appointment.getId()).child("status").setValue("Checked In")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Checked in successfully", Toast.LENGTH_SHORT).show();
                                appointment.setStatus("Checked In");
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to check in", Toast.LENGTH_SHORT).show()
                            );
                }
            }
        });

        recyclerView.setAdapter(adapter);

        // Get current user's email
        SharedPrefManager sp = new SharedPrefManager(getContext());
        String userEmailKey = sp.getUserEmail().replace(".", "_"); // Firebase cannot have dots in keys

        dbRef = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(userEmailKey);

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Appointment appointment = ds.getValue(Appointment.class);
                    if (appointment != null) {
                        // Ensure Firebase key is stored in appointment for updating status
                        appointment.setId(ds.getKey());
                        appointmentList.add(appointment);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
