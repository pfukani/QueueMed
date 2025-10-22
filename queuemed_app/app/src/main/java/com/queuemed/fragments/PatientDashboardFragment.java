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
import com.queuemed.models.Appointment;
import com.queuemed.utils.SharedPrefManager;

public class PatientDashboardFragment extends Fragment {

    private TextView tvWelcome, tvNextAppointment;
    private SharedPrefManager sp;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_dashboard, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvNextAppointment = view.findViewById(R.id.tvNextAppointment);

        sp = new SharedPrefManager(getContext());
        tvWelcome.setText("Welcome, " + sp.getUserName());

        String userEmailKey = sp.getUserEmail().replace(".", "_");
        dbRef = FirebaseDatabase.getInstance().getReference("appointments").child(userEmailKey);

        // Show next appointment (simplest: first one in list)
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Appointment appointment = ds.getValue(Appointment.class);
                        if(appointment != null && appointment.getStatus().equals("Pending")){
                            tvNextAppointment.setText("Next Appointment: " + appointment.getDate() + " at " + appointment.getTime());
                            break;
                        }
                    }
                } else {
                    tvNextAppointment.setText("No upcoming appointments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return view;
    }
}
