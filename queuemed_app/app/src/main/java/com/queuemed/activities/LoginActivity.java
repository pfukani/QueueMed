package com.queuemed.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.queuemed.R;
import com.queuemed.api.ApiClient;
import com.queuemed.api.ApiService;
import com.queuemed.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private SharedPrefManager sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = new SharedPrefManager(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> loginUser());
        // Extra links
        findViewById(R.id.tvForgot).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        findViewById(R.id.tvSignup).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("LOGIN_DEBUG", "Starting login process for: " + email);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Call<Map<String, Object>> call = api.login(body);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d("LOGIN_DEBUG", "Response received - isSuccessful: " + response.isSuccessful());

                if(response.isSuccessful() && response.body() != null){
                    Log.d("LOGIN_DEBUG", "Response body: " + response.body().toString());

                    boolean success = Boolean.parseBoolean(response.body().get("success").toString());
                    Log.d("LOGIN_DEBUG", "Login success: " + success);

                    if(success){
                        // Get user data object
                        Map<String, Object> data = (Map<String, Object>) response.body().get("data");

                        // Get ALL user fields
                        String firstName = data.get("first_name").toString();
                        String lastName = data.get("last_name").toString();
                        String email = data.get("email").toString();
                        String contact = data.get("contact_no").toString();
                        String role = data.get("role").toString();

                        // Get additional fields with null safety
                        String idNumber = data.containsKey("id_number") && data.get("id_number") != null ? data.get("id_number").toString() : "";
                        String sex = data.containsKey("sex") && data.get("sex") != null ? data.get("sex").toString() : "";
                        String age = data.containsKey("age") && data.get("age") != null ? data.get("age").toString() : "";
                        String race = data.containsKey("race") && data.get("race") != null ? data.get("race").toString() : "";
                        String language = data.containsKey("language") && data.get("language") != null ? data.get("language").toString() : "";
                        String imageUrl = data.containsKey("image_url") && data.get("image_url") != null ? data.get("image_url").toString() : "";

                        // Get user ID
                        int userId = -1;
                        if (data.containsKey("id")) {
                            try {
                                double idDouble = Double.parseDouble(data.get("id").toString());
                                userId = (int) idDouble;
                            } catch (NumberFormatException e) {
                                Log.e("LOGIN_DEBUG", "Failed to parse user ID: " + data.get("id"));
                            }
                        }

                        // DEBUG: Log what we're saving
                        Log.d("LOGIN_DEBUG", "Saving complete user info - ID: " + userId + ", Name: " + firstName + " " + lastName);

                        // Save COMPLETE user info to SharedPref
                        sp.saveUserRole(role);
                        sp.saveCompleteUserInfo(firstName, lastName, email, contact, userId, idNumber, sex, age, race, language, imageUrl);

                        // Start DashboardActivity directly
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = response.body().get("message").toString();
                        Log.d("LOGIN_DEBUG", "Login failed: " + message);
                        Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LOGIN_DEBUG", "Response not successful or body is null");
                    if (!response.isSuccessful()) {
                        Log.e("LOGIN_DEBUG", "Response code: " + response.code());
                        Log.e("LOGIN_DEBUG", "Response message: " + response.message());
                    }
                    Toast.makeText(LoginActivity.this, "Login failed: Invalid response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("LOGIN_DEBUG", "Login failed: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}