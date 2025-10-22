package com.queuemed.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.queuemed.R;
import com.queuemed.models.Appointment;
import com.queuemed.utils.SharedPrefManager;

import java.util.UUID;

public class BookAppointmentActivity extends AppCompatActivity {

    private TextInputEditText etDate, etTime;
    private MaterialButton btnSubmit;

    private DatabaseReference dbRef;
    private SharedPrefManager sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        sp = new SharedPrefManager(this);

        // Initialize views
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnSubmit = findViewById(R.id.btnSubmitAppointment);

        // Firebase reference
        String userEmailKey = sp.getUserEmail().replace(".", "_");
        dbRef = FirebaseDatabase.getInstance().getReference("appointments").child(userEmailKey);

        btnSubmit.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if(date.isEmpty() || time.isEmpty()){
                Toast.makeText(BookAppointmentActivity.this, "Please enter date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get logged-in patient info
            String patientName = sp.getUserName();
            String patientEmail = sp.getUserEmail();

            // Generate a unique ID for the appointment
            String id = UUID.randomUUID().toString();
            String status = "Pending";

            // Create Appointment object
            Appointment appointment = new Appointment(id, patientName, patientEmail, date, time, status);

            // Push to Firebase
            dbRef.push().setValue(appointment)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(BookAppointmentActivity.this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(BookAppointmentActivity.this, "Failed to book appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
