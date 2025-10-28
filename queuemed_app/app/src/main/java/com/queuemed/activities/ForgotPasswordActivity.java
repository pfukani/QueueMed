package com.queuemed.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.queuemed.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText etEmail = findViewById(R.id.etEmail);
        MaterialButton btnReset = findViewById(R.id.btnReset);

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            // Later: call API request_password_reset.php
            Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show();
        });
    }
}
