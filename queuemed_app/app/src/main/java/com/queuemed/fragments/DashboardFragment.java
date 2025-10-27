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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerAppointments, recyclerQueue;
    private AppointmentAdapter appointmentAdapter;
    private QueueAdapter queueAdapter;

    private List<Appointment> recentAppointments = new ArrayList<>();
    private List<PatientQueueItem> queueList = new ArrayList<>();

    private DatabaseReference appointmentsRef, queueRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_dashboard, container, false);

        recyclerAppointments = view.findViewById(R.id.recyclerPatientAppointments);
        recyclerQueue = view.findViewById(R.id.recyclerQueue);

        // Safety check for null RecyclerViews
        if (recyclerAppointments != null) {
            recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
            appointmentAdapter = new AppointmentAdapter(getContext(), recentAppointments, null, false);
            recyclerAppointments.setAdapter(appointmentAdapter);
        }

        if (recyclerQueue != null) {
            recyclerQueue.setLayoutManager(new LinearLayoutManager(getContext()));
            queueAdapter = new QueueAdapter(getContext(), queueList, "", patient -> { /* handle click */ });
            recyclerQueue.setAdapter(queueAdapter);
        }

        // Firebase references
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        queueRef = FirebaseDatabase.getInstance().getReference("queue");

        loadAppointments();
        loadQueue();

        return view;
    }

    private void loadAppointments() {
        if (appointmentsRef == null) return;

        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentAppointments.clear();

                for (DataSnapshot userNode : snapshot.getChildren()) {
                    for (DataSnapshot apptNode : userNode.getChildren()) {
                        // Only attempt conversion if snapshot is a Map/object
                        Object obj = apptNode.getValue();
                        if (obj instanceof Map) {
                            Appointment appointment = apptNode.getValue(Appointment.class);
                            if (appointment != null) recentAppointments.add(appointment);
                        }
                    }
                }

                if (appointmentAdapter != null) appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadQueue() {
        if (queueRef == null) return;

        queueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                queueList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Object obj = ds.getValue();
                    if (obj instanceof Map) {
                        PatientQueueItem item = ds.getValue(PatientQueueItem.class);
                        if (item != null) queueList.add(item);
                    }
                }

                if (queueAdapter != null) queueAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}

