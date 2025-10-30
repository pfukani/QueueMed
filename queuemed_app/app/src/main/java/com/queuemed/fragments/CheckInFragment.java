package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.queuemed.adapters.AppointmentAdapter;
import com.queuemed.models.Appointment;
import com.queuemed.models.PatientQueueItem;
import com.queuemed.models.QueueEntry;
import com.queuemed.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class CheckInFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private DatabaseReference dbRef;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        recyclerView = view.findViewById(R.id.recyclerPatientAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sp = new SharedPrefManager(requireContext());

        adapter = new AppointmentAdapter(
                getContext(),
                appointmentList,
                this::onCheckIn,
                true // show Check-In button
        );

        recyclerView.setAdapter(adapter);
        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        String userEmail = sp.getUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: user email not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String userEmailKey = userEmail.replace(".", "_");
        dbRef = FirebaseDatabase.getInstance().getReference("appointments").child(userEmailKey);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Appointment appointment = ds.getValue(Appointment.class);
                    if (appointment != null && "Pending".equalsIgnoreCase(appointment.getStatus())) {
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

    //  Handles Check-In
    // Handles Check-In
    private void onCheckIn(Appointment appointment) {
        String userEmail = sp.getUserEmail();
        String userName = sp.getUserName();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: user email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmailKey = userEmail.replace(".", "_");

        // Update appointment status
        DatabaseReference appointmentRef = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(userEmailKey)
                .child(appointment.getId());

        appointmentRef.child("status").setValue("Checked In")
                .addOnSuccessListener(aVoid -> {

                    // Add to queue with initial status "Waiting"
                    DatabaseReference queueRef = FirebaseDatabase.getInstance()
                            .getReference("queue");

                    long ts = System.currentTimeMillis();

                    PatientQueueItem queueItem = new PatientQueueItem(
                            appointment.getId(),
                            userName,
                            userEmail,
                            "Waiting", // initial queue status
                            ts
                    );

                    queueRef.child(appointment.getId()).setValue(queueItem)
                            .addOnSuccessListener(v ->
                                    Toast.makeText(getContext(), "Checked in and added to queue!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to add to queue: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    // Update local appointment list for instant feedback
                    appointment.setStatus("Checked In");
                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Check-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




}
