package com.queuemed.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.queuemed.R;

public class OnboardingActivity extends AppCompatActivity {

    private MaterialButton btnCreate, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize buttons
        btnCreate = findViewById(R.id.btnCreate);
        btnLogin = findViewById(R.id.btnLogin);

        // Handle Create Account click
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle Login click
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
