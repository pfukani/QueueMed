package com.queuemed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.queuemed.R;
import com.queuemed.activities.LoginActivity;
import com.queuemed.activities.ProfileActivity;
import com.queuemed.utils.SharedPrefManager;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvSurname, tvIdNumber, tvSex, tvAge, tvContact, tvRace, tvLanguage, tvEmail;
    private ImageView profileImage;
    private MaterialButton btnEditProfile, btnLogout;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        profileImage = view.findViewById(R.id.profileImage);
        tvName = view.findViewById(R.id.tvProfileName);
        tvSurname = view.findViewById(R.id.tvProfileSurname);
        tvIdNumber = view.findViewById(R.id.tvProfileIdNumber);
        tvSex = view.findViewById(R.id.tvProfileSex);
        tvAge = view.findViewById(R.id.tvProfileAge);
        tvContact = view.findViewById(R.id.tvProfileContact);
        tvRace = view.findViewById(R.id.tvProfileRace);
        tvLanguage = view.findViewById(R.id.tvProfileLanguage);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        sp = new SharedPrefManager(getContext());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutUser());

        loadUserInfo();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo(); // Refresh when returning from ProfileActivity
    }

    private void loadUserInfo() {
        // Load from SharedPreferences with proper null handling
        String firstName = sp.getUserName();
        String email = sp.getUserEmail();
        String contact = sp.getUserContact();

        // Load additional fields from SharedPreferences with null handling
        String surname = sp.getSharedPreferences().getString("last_name", "");
        String idNumber = sp.getSharedPreferences().getString("id_number", "");
        String sex = sp.getSharedPreferences().getString("sex", "");
        String age = sp.getSharedPreferences().getString("age", "");
        String race = sp.getSharedPreferences().getString("race", "");
        String language = sp.getSharedPreferences().getString("language", "");
        String imageUrl = sp.getSharedPreferences().getString("image_url", "");

        // Update TextViews - show "Not set" for empty values
        tvName.setText("Name: " + (firstName.isEmpty() ? "Not set" : firstName));
        tvSurname.setText("Surname: " + (surname.isEmpty() ? "Not set" : surname));
        tvIdNumber.setText("ID Number: " + (idNumber.isEmpty() ? "Not set" : idNumber));
        tvSex.setText("Sex: " + (sex.isEmpty() ? "Not set" : sex));
        tvAge.setText("Age: " + (age.isEmpty() ? "Not set" : age));
        tvContact.setText("Contact: " + (contact.isEmpty() ? "Not set" : contact));
        tvRace.setText("Race: " + (race.isEmpty() ? "Not set" : race));
        tvLanguage.setText("Language: " + (language.isEmpty() ? "Not set" : language));
        tvEmail.setText("Email: " + (email.isEmpty() ? "Not set" : email));

        // Load profile image
        if (!imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_heart)
                    .error(R.drawable.ic_heart)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_heart);
        }
    }

    private void logoutUser() {
        sp.logoutUser();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

}