package com.queuemed.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.models.Appointment;
import com.queuemed.utils.SharedPrefManager;

public class BookAppointmentActivity extends AppCompatActivity {

    private EditText etDate, etTime;
    private Button btnBook;
    private DatabaseReference dbRef;
    private SharedPrefManager sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        btnBook = findViewById(R.id.btnBook);

        sp = new SharedPrefManager(this);

        // Use email as Firebase key (dots replaced with _)
        String userEmailKey = sp.getUserEmail().replace(".", "_");
        dbRef = FirebaseDatabase.getInstance().getReference("appointments").child(userEmailKey);

        btnBook.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please enter date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            String patientName = sp.getUserName();
            String patientEmail = sp.getUserEmail();
            String status = "pending"; // default status
            String id = dbRef.push().getKey(); // generate unique Firebase key

            Appointment appointment = new Appointment(id, patientName, patientEmail, date, time, status);
            bookAppointment(appointment);
        });
    }

    private void bookAppointment(Appointment newAppointment) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean conflict = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Appointment existing = ds.getValue(Appointment.class);
                    if (existing != null &&
                            existing.getDate().equals(newAppointment.getDate()) &&
                            existing.getTime().equals(newAppointment.getTime())) {
                        conflict = true;
                        break;
                    }
                }

                if (conflict) {
                    Toast.makeText(BookAppointmentActivity.this,
                            "You already have an appointment at this time!", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the appointment
                    dbRef.child(newAppointment.getId()).setValue(newAppointment)
                            .addOnSuccessListener(aVoid -> Toast.makeText(BookAppointmentActivity.this,
                                    "Appointment booked successfully!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(BookAppointmentActivity.this,
                                    "Failed to book appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookAppointmentActivity.this,
                        "Failed to check existing appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
