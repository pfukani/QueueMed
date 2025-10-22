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

public class CheckInFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointmentList;
    private SharedPrefManager sp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        sp = new SharedPrefManager(requireContext());

        recyclerView = view.findViewById(R.id.recyclerCheckIn);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        appointmentList = new ArrayList<>();
        adapter = new AppointmentsAdapter(getContext(), appointmentList, this::checkInAppointment);
        recyclerView.setAdapter(adapter);

        loadAppointments();

        return view;
    }

    private void loadAppointments() {
        String patientEmail = sp.getUserEmail();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("appointments");

        dbRef.orderByChild("patientEmail").equalTo(patientEmail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Appointment appointment = dataSnapshot.getValue(Appointment.class);
                            if (appointment != null) {
                                appointmentList.add(appointment);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load appointments: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkInAppointment(Appointment appointment) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("appointments");
        dbRef.child(appointment.getId()).child("status").setValue("Checked-In")
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Checked in successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Check-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
