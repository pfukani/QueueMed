package com.queuemed.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.queuemed.R;
import com.queuemed.fragments.PatientDashboardFragment;

public class PatientDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Load the patient dashboard fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new PatientDashboardFragment())
                .commit();
    }
}
