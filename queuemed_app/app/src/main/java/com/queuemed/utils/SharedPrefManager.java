package com.queuemed.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "QueueMedPref";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_CONTACT = "user_contact";

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

    // Save basic user info: name, email, contact
    public void saveUserInfo(String name, String email, String contact) {
        if (name != null) editor.putString(KEY_NAME, name);
        if (email != null) editor.putString(KEY_EMAIL, email);
        if (contact != null) editor.putString(KEY_CONTACT, contact);
        editor.apply();
    }

    // Getters
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

    // Clear all saved preferences
    public void clear() {
        editor.clear().apply();
    }
}
