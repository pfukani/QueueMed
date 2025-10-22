package com.queuemed.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.queuemed.R;
import com.queuemed.fragments.BookAppointmentFragment;
import com.queuemed.fragments.DashboardFragment;
import com.queuemed.fragments.MedicalHistoryFragment;
import com.queuemed.fragments.ProfileFragment;
import com.queuemed.fragments.QueueFragment;
import com.queuemed.fragments.VitalsFragment;
import com.queuemed.fragments.CheckInFragment;
import com.queuemed.utils.SharedPrefManager;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private SharedPrefManager sp;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sp = new SharedPrefManager(this);
        role = sp.getUserRole(); // "patient" or "staff"

        bottomNav = findViewById(R.id.bottomNav);

        // Inflate correct menu and default fragment
        if (role.equals("staff")) {
            bottomNav.inflateMenu(R.menu.bottom_nav_staff);
            loadFragment(new QueueFragment()); // default fragment for staff
        } else { // patient
            bottomNav.inflateMenu(R.menu.bottom_nav_patient);
            loadFragment(new DashboardFragment()); // default fragment for patient
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (role.equals("staff")) {
                if (id == R.id.nav_queue) {
                    fragment = new QueueFragment();
                } else if (id == R.id.nav_update_vitals) {
                    fragment = new VitalsFragment();
                } else if (id == R.id.nav_patient_records) {
                    fragment = new MedicalHistoryFragment();
                } else if (id == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                } else if (id == R.id.nav_logout) {
                    sp.clear();
                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    finish();
                }
            } else { // patient
                if (id == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.nav_appointments) {
                    fragment = new BookAppointmentFragment();
                } else if (id == R.id.nav_checkin) {
                    fragment = new CheckInFragment();
                } else if (id == R.id.nav_vitals) {
                    fragment = new VitalsFragment();
                } else if (id == R.id.nav_history) {
                    fragment = new MedicalHistoryFragment();
                } else if (id == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                }
            }

            if (fragment != null) loadFragment(fragment);
            return true;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
