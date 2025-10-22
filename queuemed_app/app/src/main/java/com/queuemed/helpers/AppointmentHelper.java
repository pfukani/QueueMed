package com.queuemed.helpers;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AppointmentHelper {

    private final DatabaseReference databaseReference;

    public AppointmentHelper() {
        // Reference to "appointments" node in Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments");
    }

    public void addAppointment(String userId, String doctor, String date, String time) {
        String appointmentId = databaseReference.push().getKey(); // Generate unique ID

        Appointment appointment = new Appointment(doctor, date, time, "pending");

        assert appointmentId != null;
        databaseReference.child(userId).child(appointmentId).setValue(appointment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firebase", " Appointment added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", " Error adding appointment", e);
                    }
                });
    }

    // Model class for appointment
    public static class Appointment {
        public String doctor;
        public String date;
        public String time;
        public String status;

        public Appointment() {
            // Default constructor required for calls to DataSnapshot.getValue()
        }

        public Appointment(String doctor, String date, String time, String status) {
            this.doctor = doctor;
            this.date = date;
            this.time = time;
            this.status = status;
        }
    }
}
