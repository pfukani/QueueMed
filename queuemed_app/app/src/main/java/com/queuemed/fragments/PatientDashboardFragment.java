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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardFragment extends Fragment {

    private TextView tvWelcome, tvUpcomingAppointments, tvCheckedIn, tvNextAppointment;
    private RecyclerView recyclerPatientAppointments;
    private List<Appointment> appointmentList;
    private AppointmentAdapter adapter;

    private DatabaseReference appointmentsRef;
    private FirebaseUser currentUser;

    // Replace with your XAMPP API URL
    private static final String BASE_URL = "http://10.0.2.2/queuemed_api/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_dashboard, container, false);

        // UI elements
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvUpcomingAppointments = view.findViewById(R.id.tvUpcomingAppointments);
        tvCheckedIn = view.findViewById(R.id.tvCheckedIn);
        tvNextAppointment = view.findViewById(R.id.tvNextAppointment);
        recyclerPatientAppointments = view.findViewById(R.id.recyclerRecentAppointments);

        // RecyclerView setup
        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(getContext(), appointmentList, null, false);
        recyclerPatientAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPatientAppointments.setAdapter(adapter);

        // Get logged-in Firebase user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            loadUserNameFromMySQL(email); // Load full name from MySQL
            loadAppointments(email);      // Load appointments from Firebase
        } else {
            tvWelcome.setText("Welcome User");
        }

        return view;
    }

    // Fetch the user's full name from MySQL database
    private void loadUserNameFromMySQL(String email) {
        // Replace with your XAMPP API URL
        String url = "http://10.0.2.2/queueMedAPI/getUser.php?email=" + email;

        // Create Volley request queue
        RequestQueue queue = Volley.newRequestQueue(getContext());

        // Create JsonObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        System.out.println("Volley Response: " + response.toString()); // Debug

                        // Extract first and last name
                        String firstName = response.getString("first_name");
                        String lastName = response.getString("last_name");

                        // Update welcome message
                        tvWelcome.setText("Welcome, " + firstName + " " + lastName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvWelcome.setText("Welcome User");
                    }
                },
                error -> {
                    error.printStackTrace();
                    tvWelcome.setText("Welcome User");
                }
        );

        // Add request to the queue
        queue.add(request);
    }


    // Load appointments from Firebase
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
                            nextAppointment.getDate() + " at " +
                                    nextAppointment.getTime() + " with Dr. " +
                                    nextAppointment.getDoctorName()
                    );
                } else {
                    tvNextAppointment.setText("No upcoming appointments");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle Firebase database error if needed
            }
        });
    }
}
