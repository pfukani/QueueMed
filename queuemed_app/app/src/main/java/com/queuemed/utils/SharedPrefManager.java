package com.queuemed.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefManager {

    private static final String PREF_NAME = "QueueMedPref";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_CONTACT = "user_contact";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save user role
    public void saveUserRole(String role) {
        if (role != null) {
            editor.putString(KEY_ROLE, role);
            editor.apply();
        }
    }
    // Save complete user info including all profile fields
    public void saveCompleteUserInfo(String firstName, String lastName, String email, String contact,
                                     int userId, String idNumber, String sex, String age,
                                     String race, String language, String imageUrl) {
        editor.putString(KEY_NAME, firstName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_CONTACT, contact);
        editor.putInt(KEY_USER_ID, userId);

        // Save additional profile fields
        editor.putString("last_name", lastName);
        editor.putString("id_number", idNumber != null ? idNumber : "");
        editor.putString("sex", sex != null ? sex : "");
        editor.putString("age", age != null ? age : "");
        editor.putString("race", race != null ? race : "");
        editor.putString("language", language != null ? language : "");
        editor.putString("image_url", imageUrl != null ? imageUrl : "");

        editor.apply();
    }
    public void saveUserInfo(String name, String email, String contact, int userId) {
        // Use the instance editor, don't create a new one
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_CONTACT, contact);
        editor.putInt(KEY_USER_ID, userId);
        editor.apply(); // Use apply() for async saving
    }

    // Get user ID
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Getters with default values
    public String getUserRole() {
        return prefs.getString(KEY_ROLE, "patient");
    }

    public String getUserName() {
        return prefs.getString(KEY_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, "user@email.com");
    }

    public String getUserContact() {
        return prefs.getString(KEY_CONTACT, "0000000000");
    }

    // Clear all saved preferences (logout)
    public void logoutUser() {
        editor.clear().apply();
    }

    // Get SharedPreferences instance
    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID) && getUserId() != -1;
    }
}