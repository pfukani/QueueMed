package com.queuemed.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.queuemed.R;
import com.queuemed.utils.SharedPrefManager;
import com.queuemed.utils.VolleyMultipartRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText txtName, txtSurname, txtID, txtSex, txtAge, txtContact, txtRace, txtLanguage, txtEmail;
    ImageView profileImage;
    Button btnEdit, btnSave, btnUpload;
    ImageButton btnBack;
    Uri imageUri;
    final int PICK_IMAGE = 1;

    private SharedPrefManager sp;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Use SharedPrefManager instead of direct SharedPreferences
        sp = new SharedPrefManager(this);
        userId = sp.getUserId(); // Get user ID from SharedPrefManager

        txtName = findViewById(R.id.txtName);
        txtSurname = findViewById(R.id.txtSurname);
        txtID = findViewById(R.id.txtID);
        txtSex = findViewById(R.id.txtSex);
        txtAge = findViewById(R.id.txtAge);
        txtContact = findViewById(R.id.txtContact);
        txtRace = findViewById(R.id.txtRace);
        txtLanguage = findViewById(R.id.txtLanguage);
        txtEmail = findViewById(R.id.txtEmail);
        profileImage = findViewById(R.id.profileImage);

        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setVisibility(View.GONE);
        btnBack = findViewById(R.id.btnBack);

        Log.d("PROFILE_ACTIVITY", "User ID: " + userId); // Should show actual user ID now

        loadProfileLocally();
        setFieldsEditable(false);

        btnEdit.setOnClickListener(v -> {
            setFieldsEditable(true);
            btnUpload.setVisibility(View.VISIBLE);
        });

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfileToServer();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void uploadProfileImage(Bitmap bitmap, int userId) {
        String uploadUrl = "http://10.0.2.2/queuemed_api/upload_image.php";

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                uploadUrl,
                response -> {
                    try {
                        String json = new String(response.data);
                        JSONObject obj = new JSONObject(json);
                        if (obj.getString("status").equals("success")) {
                            String imageUrl = obj.getString("image_url");

                            // Save the URL locally using SharedPrefManager
                            sp.getSharedPreferences().edit().putString("image_url", imageUrl).apply();

                            // Display image
                            Glide.with(this).load(imageUrl).into(profileImage);

                            Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            Log.d("UPLOAD", "URL: " + imageUrl);
                        } else {
                            Toast.makeText(this, "Upload failed: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Response parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Upload error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        // Add form-data fields
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(userId));
        volleyMultipartRequest.setParams(params);

        // Add file data
        Map<String, VolleyMultipartRequest.DataPart> data = new HashMap<>();
        data.put("image", new VolleyMultipartRequest.DataPart(
                "profile_" + userId + ".jpg",
                getFileDataFromBitmap(bitmap),
                "image/jpeg"
        ));
        volleyMultipartRequest.setByteData(data);

        Log.d("UPLOAD", "User ID: " + userId); // Should show actual user ID now

        // Add to request queue
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    private byte[] getFileDataFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void updateProfileToServer() {
        String url = "http://10.0.2.2/queuemed_api/update_profile.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("UPDATE_PROFILE", "Server response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                            setFieldsEditable(false);
                            btnUpload.setVisibility(View.GONE);
                            saveProfileLocally();
                        } else {
                            Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setFieldsEditable(false);
                        btnUpload.setVisibility(View.GONE);
                        saveProfileLocally();
                    }
                },
                error -> {
                    Log.e("UPDATE_PROFILE", "Error: " + error.getMessage());
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Use the user ID from SharedPrefManager
                params.put("id", String.valueOf(userId));
                params.put("first_name", txtName.getText().toString());
                params.put("last_name", txtSurname.getText().toString());
                params.put("id_number", txtID.getText().toString());
                params.put("sex", txtSex.getText().toString());
                params.put("age", txtAge.getText().toString());
                params.put("contact_no", txtContact.getText().toString());
                params.put("race", txtRace.getText().toString());
                params.put("language", txtLanguage.getText().toString());
                params.put("email", txtEmail.getText().toString());

                Log.d("UPDATE_PROFILE", "Sending params: " + params.toString());
                Log.d("UPDATE_PROFILE", "User ID: " + userId); // Should show actual user ID now

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setFieldsEditable(boolean enabled) {
        txtName.setEnabled(enabled);
        txtSurname.setEnabled(enabled);
        txtID.setEnabled(enabled);
        txtSex.setEnabled(enabled);
        txtAge.setEnabled(enabled);
        txtContact.setEnabled(enabled);
        txtRace.setEnabled(enabled);
        txtLanguage.setEnabled(enabled);
        txtEmail.setEnabled(enabled);
    }

    private void saveProfileLocally() {
        // Save to SharedPrefManager's SharedPreferences
        sp.getSharedPreferences().edit()
                .putString("first_name", txtName.getText().toString())
                .putString("last_name", txtSurname.getText().toString())
                .putString("id_number", txtID.getText().toString())
                .putString("sex", txtSex.getText().toString())
                .putString("age", txtAge.getText().toString())
                .putString("contact_no", txtContact.getText().toString())
                .putString("race", txtRace.getText().toString())
                .putString("language", txtLanguage.getText().toString())
                .putString("email", txtEmail.getText().toString())
                .apply();

        // ALSO UPDATE THE MAIN USER NAME IN SharedPrefManager
        sp.saveUserInfo(txtName.getText().toString(), txtEmail.getText().toString(),
                txtContact.getText().toString(), userId);
    }

    private void loadProfileLocally() {
        // Load from SharedPrefManager's SharedPreferences
        txtName.setText(sp.getSharedPreferences().getString("first_name", ""));
        txtSurname.setText(sp.getSharedPreferences().getString("last_name", ""));
        txtID.setText(sp.getSharedPreferences().getString("id_number", ""));
        txtSex.setText(sp.getSharedPreferences().getString("sex", ""));
        txtAge.setText(sp.getSharedPreferences().getString("age", ""));
        txtContact.setText(sp.getSharedPreferences().getString("contact_no", ""));
        txtRace.setText(sp.getSharedPreferences().getString("race", ""));
        txtLanguage.setText(sp.getSharedPreferences().getString("language", ""));
        txtEmail.setText(sp.getSharedPreferences().getString("email", ""));

        String imageUrl = sp.getSharedPreferences().getString("image_url", "");
        if (!imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(profileImage);
        }
    }

    private boolean validateInputs() {
        String id = txtID.getText().toString();

        if (txtName.getText().toString().isEmpty() || id.length() != 13) {
            Toast.makeText(this, "Invalid ID or missing name", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Calculate RSA ID age
        int birthYear = Integer.parseInt(id.substring(0, 2));
        int birthMonth = Integer.parseInt(id.substring(2, 4));
        int birthDay = Integer.parseInt(id.substring(4, 6));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        birthYear += (birthYear <= currentYear % 100) ? 2000 : 1900;
        int age = currentYear - birthYear;
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--;
        }
        txtAge.setText(String.valueOf(age));

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    profileImage.setImageBitmap(bitmap);
                    // Upload immediately after selection
                    uploadProfileImage(bitmap, userId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
            }
        }
    }
}