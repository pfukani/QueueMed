package com.queuemed.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.queuemed.R;
import com.queuemed.utils.SharedPrefManager;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvContact;
    private MaterialButton btnEdit;
    private SharedPrefManager sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvContact = view.findViewById(R.id.tvProfileContact);
        btnEdit = view.findViewById(R.id.btnEditProfile);
        sp = new SharedPrefManager(requireContext());

        loadUserInfo();

        btnEdit.setOnClickListener(v -> showEditDialog());

        return view;
    }

    private void loadUserInfo() {
        tvName.setText("Name: " + sp.getUserName());
        tvEmail.setText("Email: " + sp.getUserEmail());
        tvContact.setText("Contact: " + sp.getUserContact());
    }

    private void showEditDialog() {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        EditText etContact = dialogView.findViewById(R.id.etEditContact);

        // Pre-fill with existing data
        etName.setText(sp.getUserName());
        etEmail.setText(sp.getUserEmail());
        etContact.setText(sp.getUserContact());

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            MaterialButton saveBtn = (MaterialButton) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String contact = etContact.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Use your existing method
                sp.saveUserInfo(name, email, contact);

                loadUserInfo();
                Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
