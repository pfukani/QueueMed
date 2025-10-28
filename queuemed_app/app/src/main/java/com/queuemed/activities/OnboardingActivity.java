package com.queuemed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.queuemed.R;
import com.queuemed.utils.SharedPrefManager;

public class OnboardingActivity extends AppCompatActivity {

    private MaterialButton btnCreate, btnLogin;
    private SharedPrefManager sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = new SharedPrefManager(this);

        // DEBUG: Check what's stored in SharedPreferences
        Log.d("ONBOARDING_DEBUG", "User ID: " + sp.getUserId());
        Log.d("ONBOARDING_DEBUG", "User Name: " + sp.getUserName());
        Log.d("ONBOARDING_DEBUG", "User Email: " + sp.getUserEmail());
        Log.d("ONBOARDING_DEBUG", "isLoggedIn(): " + sp.isLoggedIn());

        // Check if user is already logged in
        if (sp.isLoggedIn()) {
            Log.d("ONBOARDING_DEBUG", "User is logged in, redirecting to Dashboard");
            // User is logged in, go directly to Dashboard
            Intent intent = new Intent(OnboardingActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("ONBOARDING_DEBUG", "User is NOT logged in, showing onboarding");
        }

        // User is not logged in, show onboarding screen
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