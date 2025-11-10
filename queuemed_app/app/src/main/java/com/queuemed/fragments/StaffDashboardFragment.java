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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.adapters.AppointmentAdapter;
import com.queuemed.adapters.QueueAdapter;
import com.queuemed.models.Appointment;
import com.queuemed.models.PatientQueueItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StaffDashboardFragment extends Fragment {

    private TextView tvQueueCount, tvAppointmentsCount;
    private RecyclerView recyclerAppointments, recyclerQueue;
    private AppointmentAdapter appointmentAdapter;
    private QueueAdapter queueAdapter;

    private List<Appointment> appointmentList = new ArrayList<>();
    private List<PatientQueueItem> queueList = new ArrayList<>();

    private DatabaseReference dbAppointmentsRef, dbQueueRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_dashboard, container, false);

        tvQueueCount = view.findViewById(R.id.tvQueueCount);
        tvAppointmentsCount = view.findViewById(R.id.tvAppointmentsCount);
        recyclerAppointments = view.findViewById(R.id.recyclerStaffAppointments);
        recyclerQueue = view.findViewById(R.id.recyclerQueueList);

        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerQueue.setLayoutManager(new LinearLayoutManager(getContext()));

        dbAppointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        dbQueueRef = FirebaseDatabase.getInstance().getReference("queue");

        // FIX: Use the correct adapter setup for staff
        setupAdapters();

        refreshData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        loadAppointments();
        loadQueue();
    }
    private void loadAppointments() {
        ValueEventListener appointmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();

                int bookedCount = 0;
                int checkedInCount = 0;
                int completedCount = 0;

                for (DataSnapshot userNode : snapshot.getChildren()) {
                    for (DataSnapshot apptNode : userNode.getChildren()) {
                        Object raw = apptNode.getValue();
                        if (raw instanceof Map) {
                            Appointment appointment = apptNode.getValue(Appointment.class);
                            if (appointment != null) {
                                // FIX: Don't modify the original appointment object for display
                                Appointment displayAppointment = new Appointment(
                                        appointment.getId(),
                                        "See Doctor", // Always show "See Doctor" for staff
                                        appointment.getPatientName(),
                                        appointment.getPatientEmail(),
                                        appointment.getDate(),
                                        appointment.getTime(),
                                        appointment.getStatus()
                                );
                                appointmentList.add(displayAppointment);

                                // Count by status - include both "Booked" and "Pending"
                                String status = appointment.getStatus();
                                if ("Booked".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
                                    bookedCount++;
                                } else if ("Checked In".equalsIgnoreCase(status)) {
                                    checkedInCount++;
                                } else if ("Completed".equalsIgnoreCase(status)) {
                                    completedCount++;
                                }
                            }
                        }
                    }
                }

                if (appointmentAdapter != null) appointmentAdapter.notifyDataSetChanged();

                if (tvAppointmentsCount != null) {
                    tvAppointmentsCount.setText(
                            "Appointments - Booked: " + bookedCount +
                                    " | Checked In: " + checkedInCount +
                                    " | Completed: " + completedCount
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        dbAppointmentsRef.addValueEventListener(appointmentsListener);
    }

    private void setupAdapters() {
        // Appointment adapter for staff
        appointmentAdapter = new AppointmentAdapter(
                getContext(),
                appointmentList,
                null,           // No check-in callback for staff
                false           // Don't show check-in button
        );
        appointmentAdapter.setStaffView(true); // Mark this as staff view
        recyclerAppointments.setAdapter(appointmentAdapter);

        // Queue adapter remains the same
        queueAdapter = new QueueAdapter(getContext(), queueList, "", patient -> openPatientDetails(patient));
        recyclerQueue.setAdapter(queueAdapter);
    }

    private void loadQueue() {
        ValueEventListener queueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                queueList.clear();

                int waitingCount = 0;
                int ongoingCount = 0;
                int completedCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PatientQueueItem item = ds.getValue(PatientQueueItem.class);
                    if (item != null) {
                        queueList.add(item);

                        // Count by status
                        if ("Waiting".equalsIgnoreCase(item.getStatus())) {
                            waitingCount++;
                        } else if ("Ongoing".equalsIgnoreCase(item.getStatus())) {
                            ongoingCount++;
                        } else if ("Completed".equalsIgnoreCase(item.getStatus())) {
                            completedCount++;
                        }
                    }
                }

                if (queueAdapter != null) queueAdapter.notifyDataSetChanged();

                // Update the TextView with a breakdown
                if (tvQueueCount != null) {
                    tvQueueCount.setText(
                            "Queue - Waiting: " + waitingCount +
                                    " | Ongoing: " + ongoingCount +
                                    " | Completed: " + completedCount
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        dbQueueRef.addValueEventListener(queueListener);
    }
    private void checkInAppointment(Appointment appointment) {
        appointment.setStatus("Checked In");

        String today = LocalDate.now().toString();
        DatabaseReference dbAppointment = dbAppointmentsRef.child(appointment.getPatientEmail().replace(".", "_")).child(appointment.getId());
        DatabaseReference dbQueueToday = dbQueueRef.child(today);

        // Create queue item
        // Generate a unique appointment ID (if you don’t already have one)
        String appointmentId = UUID.randomUUID().toString();

// Create a PatientQueueItem with all 5 parameters
        PatientQueueItem queueItem = new PatientQueueItem(
                appointmentId,
                appointment.getPatientName(),
                appointment.getPatientEmail(),
                "Waiting",
                System.currentTimeMillis()
        );


// Push to Firebase queue
        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("queue");
        String queueId = queueRef.push().getKey();

        if (queueId != null) {
            queueRef.child(queueId).setValue(queueItem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Patient added to queue", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add to queue", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Error generating queue ID", Toast.LENGTH_SHORT).show();
        }



        dbQueueToday.push().setValue(queueItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dbAppointment.setValue(appointment).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Patient Checked-In: " + appointment.getPatientName(), Toast.LENGTH_SHORT).show();
                    refreshData();
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update appointment", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Failed to add to queue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPatientDetails(PatientQueueItem patient) {
        PatientDetailsFragment fragment = PatientDetailsFragment.newInstance(
                patient.getPatientName(),
                patient.getPatientEmail()
        );

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
