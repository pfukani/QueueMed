package com.queuemed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.queuemed.R;
import com.queuemed.api.ApiClient;
import com.queuemed.api.ApiService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etFirst, etLast, etEmail, etContact, etPassword, etConfirm;
    private ProgressBar pbStrength;
    private TextView tvPasswordStrength, tvConfirmWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFirst = findViewById(R.id.etFirst);
        etLast = findViewById(R.id.etLast);
        etEmail = findViewById(R.id.etEmail);
        etContact = findViewById(R.id.etContact);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);

        pbStrength = findViewById(R.id.pbStrength); // Add this to your XML if not already
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        tvConfirmWarning = findViewById(R.id.tvConfirmWarning);

        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());

        // Real-time password strength checker
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int score = calculateStrength(s.toString());
                if (pbStrength != null) pbStrength.setProgress(score);

                if (score < 40) {
                    pbStrength.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    tvPasswordStrength.setText("Weak");
                    tvPasswordStrength.setTextColor(Color.RED);
                } else if (score < 70) {
                    pbStrength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Orange
                    tvPasswordStrength.setText("Medium");
                    tvPasswordStrength.setTextColor(Color.parseColor("#FFA500"));
                } else {
                    pbStrength.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                    tvPasswordStrength.setText("Strong");
                    tvPasswordStrength.setTextColor(Color.GREEN);
                }

                // Confirm password live check
                if (!etConfirm.getText().toString().equals(s.toString()) && !etConfirm.getText().toString().isEmpty()) {
                    tvConfirmWarning.setText("Passwords do not match");
                    tvConfirmWarning.setVisibility(TextView.VISIBLE);
                } else {
                    tvConfirmWarning.setVisibility(TextView.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        etConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(etPassword.getText().toString())) {
                    tvConfirmWarning.setText("Passwords do not match");
                    tvConfirmWarning.setVisibility(TextView.VISIBLE);
                } else {
                    tvConfirmWarning.setVisibility(TextView.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void registerUser() {
        String first = etFirst.getText().toString().trim();
        String last = etLast.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isStrongPassword(password)) {
            Toast.makeText(this,
                    "Password must be at least 8 characters long and include:\n" +
                            "• 1 uppercase letter\n" +
                            "• 1 lowercase letter\n" +
                            "• 1 number\n" +
                            "• 1 special character",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("first_name", first);
        body.put("last_name", last);
        body.put("email", email);
        body.put("contact_no", contact);
        body.put("password", password);

        Call<Map<String, Object>> call = api.register(body);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    if (success) {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error: " + response.body().get("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Response not successful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Strong password validator
    private boolean isStrongPassword(String password) {
        Pattern pattern = Pattern.compile(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        );
        return pattern.matcher(password).matches();
    }

    // Password strength score (0-100)
    private int calculateStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score += 25;
        if (password.matches(".*[A-Z].*")) score += 25;
        if (password.matches(".*[0-9].*")) score += 25;
        if (password.matches(".*[@$!%*?&].*")) score += 25;
        return score;
    }
}
