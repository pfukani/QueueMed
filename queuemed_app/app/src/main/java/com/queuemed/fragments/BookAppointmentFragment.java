package com.queuemed.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.queuemed.R;
import com.queuemed.models.Appointment;
import com.queuemed.utils.SharedPrefManager;

import java.util.Calendar;
import java.util.UUID;

public class BookAppointmentFragment extends Fragment {

    private TextInputEditText etDate, etTime;
    private MaterialButton btnBook;
    private SharedPrefManager sp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        // Initialize views
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        btnBook = view.findViewById(R.id.btnBook); // Updated ID
        sp = new SharedPrefManager(requireContext());

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Submit appointment
        btnBook.setOnClickListener(v -> submitAppointment());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int hour = hourOfDay % 12;
                    if (hour == 0) hour = 12;
                    String selectedTime = String.format("%02d:%02d %s", hour, minute, amPm);
                    etTime.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void submitAppointment() {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String patientName = sp.getUserName();
        String patientEmail = sp.getUserEmail();

        if (TextUtils.isEmpty(date)) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(time)) {
            Toast.makeText(getContext(), "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();
        String status = "Pending";

        Appointment appointment = new Appointment(id, patientName, patientEmail, date, time, status);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("appointments");
        dbRef.child(id).setValue(appointment)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Appointment booked successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to book appointment: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}