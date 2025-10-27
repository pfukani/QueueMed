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
import com.queuemed.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvQueueCount, tvAppointmentsCount;
    private RecyclerView recyclerAppointments, recyclerQueue;
    private AppointmentAdapter appointmentAdapter;
    private QueueAdapter queueAdapter;

    private List<Appointment> appointmentList = new ArrayList<>();
    private List<PatientQueueItem> queueList = new ArrayList<>();

    private DatabaseReference dbAppointmentsRef, dbQueueRef;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_dashboard, container, false);

        sp = new SharedPrefManager(getContext());

        tvQueueCount = view.findViewById(R.id.tvQueueCount);
        tvAppointmentsCount = view.findViewById(R.id.tvUpcomingAppointments);
        recyclerAppointments = view.findViewById(R.id.recyclerRecentAppointments);
        recyclerQueue = view.findViewById(R.id.recyclerQueue);

        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerQueue.setLayoutManager(new LinearLayoutManager(getContext()));

        dbAppointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        dbQueueRef = FirebaseDatabase.getInstance().getReference("queue");

        appointmentAdapter = new AppointmentAdapter(getContext(), appointmentList, null, false);
        recyclerAppointments.setAdapter(appointmentAdapter);

        queueAdapter = new QueueAdapter(getContext(), queueList, "", null);
        recyclerQueue.setAdapter(queueAdapter);

        refreshData();

        return view;
    }

    private void refreshData() {
        loadAppointments();
        loadQueue();
    }

    private void loadAppointments() {
        final String userEmailKey = sp.getUserEmail() != null ? sp.getUserEmail().replace(".", "_") : "";

        dbAppointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                int totalAppointments = 0;

                for (DataSnapshot apptNode : snapshot.getChildren()) {
                    Appointment appointment = apptNode.getValue(Appointment.class);
                    if (appointment != null) {
                        String apptEmail = appointment.getPatientEmail();
                        if (apptEmail != null && apptEmail.replace(".", "_").equals(userEmailKey)) {
                            appointmentList.add(appointment);
                            totalAppointments++;
                        }
                    }
                }

                tvAppointmentsCount.setText("Appointments: " + totalAppointments);
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQueue() {
        final String userEmailKey = sp.getUserEmail() != null ? sp.getUserEmail().replace(".", "_") : "";

        dbQueueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                queueList.clear();
                int queueCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PatientQueueItem item = ds.getValue(PatientQueueItem.class);
                    if (item != null) {
                        String patientEmail = item.getPatientEmail();
                        if (patientEmail != null && patientEmail.replace(".", "_").equals(userEmailKey)) {
                            queueList.add(item);
                            queueCount++;
                        }
                    }
                }

                tvQueueCount.setText("Queue: " + queueCount);
                queueAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load queue", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
