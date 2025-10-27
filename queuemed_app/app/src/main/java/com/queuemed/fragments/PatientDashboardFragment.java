package com.queuemed.fragments;

import android.annotation.SuppressLint;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.adapters.AppointmentAdapter;
import com.queuemed.models.Appointment;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardFragment extends Fragment {

    private TextView tvWelcome, tvUpcomingAppointments, tvCheckedIn, tvNextAppointment;
    private RecyclerView recyclerPatientAppointments;
    private List<Appointment> appointmentList;
    private AppointmentAdapter adapter;

    private DatabaseReference appointmentsRef;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_dashboard, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvUpcomingAppointments = view.findViewById(R.id.tvUpcomingAppointments);
        tvCheckedIn = view.findViewById(R.id.tvCheckedIn);
        tvNextAppointment = view.findViewById(R.id.tvNextAppointment);
        recyclerPatientAppointments = view.findViewById(R.id.recyclerRecentAppointments);

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(getContext(), appointmentList, null, false);
        recyclerPatientAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPatientAppointments.setAdapter(adapter);

        // Get logged-in patient
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tvWelcome.setText("Welcome, " + currentUser.getDisplayName());
            loadAppointments(currentUser.getEmail());
        }

        return view;
    }

    private void loadAppointments(String patientEmail) {
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                int checkedInCount = 0;
                Appointment nextAppointment = null;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Appointment appointment = ds.getValue(Appointment.class);
                    if (appointment != null && patientEmail.equalsIgnoreCase(appointment.getPatientEmail())) {
                        appointmentList.add(appointment);

                        if ("Checked In".equalsIgnoreCase(appointment.getStatus())) {
                            checkedInCount++;
                        }

                        if (nextAppointment == null && "Pending".equalsIgnoreCase(appointment.getStatus())) {
                            nextAppointment = appointment;
                        }
                    }
                }

                // Update UI
                tvUpcomingAppointments.setText(String.valueOf(appointmentList.size()));
                tvCheckedIn.setText(String.valueOf(checkedInCount));

                if (nextAppointment != null) {
                    tvNextAppointment.setText(
                            nextAppointment.getDate() + " at " + nextAppointment.getTime() +
                                    " with Dr. " + nextAppointment.getDoctorName()
                    );
                } else {
                    tvNextAppointment.setText("No upcoming appointments");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
            }
        });
    }
}
