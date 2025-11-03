package com.queuemed.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.queuemed.R;
import com.queuemed.fragments.BookAppointmentFragment;
import com.queuemed.fragments.DashboardFragment;
import com.queuemed.fragments.PatientHistoryFragment;
import com.queuemed.fragments.ProfileFragment;
import com.queuemed.fragments.ReportsFragment;
import com.queuemed.fragments.StaffDashboardFragment;
import com.queuemed.fragments.StaffMedicalHistoryFragment;
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

        // Inflate the proper menu
        if (role.equals("staff")) {
            bottomNav.inflateMenu(R.menu.bottom_nav_staff);
            // Set default selected item for staff
            bottomNav.setSelectedItemId(R.id.nav_queue); // Or whatever should be default
            loadFragment(new StaffDashboardFragment()); // Default for staff
        } else { // patient
            bottomNav.inflateMenu(R.menu.bottom_nav_patient);
            // Set default selected item for patient
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
            loadFragment(new DashboardFragment()); // Default for patient
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (role.equals("staff")) {
                if (id == R.id.nav_staff_dashboard) {
                    fragment = new StaffDashboardFragment();

                } else if (id == R.id.nav_patient_records) {
                    fragment = new StaffMedicalHistoryFragment();
                } else if (id == R.id.nav_reports) {
                    fragment = new ReportsFragment();
                }else if (id == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                } else if (id == R.id.nav_logout) {
                    sp.logoutUser();
                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    finish();
                    return true; // Return early since we're finishing the activity
                }
            } else { // Patient menu items
                if (id == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.nav_appointments) {
                    fragment = new BookAppointmentFragment();
                } else if (id == R.id.nav_checkin) {
                    fragment = new CheckInFragment();
                } else if (id == R.id.nav_vitals) {
                    fragment = new VitalsFragment();
                } else if (id == R.id.nav_history) {
                    fragment = new PatientHistoryFragment();
                } else if (id == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                }
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

        // Refresh dashboard when reselected
        bottomNav.setOnItemReselectedListener(item -> {
            int id = item.getItemId();
            if ("staff".equals(role) && id == R.id.nav_staff_dashboard) {
                loadFragment(new StaffDashboardFragment());
            } else if (!"staff".equals(role) && id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
