package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvContact = view.findViewById(R.id.tvProfileContact);

        SharedPrefManager sp = new SharedPrefManager(getContext());

        tvName.setText("Name: " + sp.getUserName());
        tvEmail.setText("Email: " + sp.getUserEmail());
        tvContact.setText("Contact: " + sp.getUserContact());

        return view;
    }


    private void loadUserInfo() {
        String name = sp.getUserName();
        String email = sp.getUserEmail();
        String contact = sp.getUserContact();

        tvName.setText("Name: " + name);
        tvEmail.setText("Email: " + email);
        tvContact.setText("Contact: " + contact);
    }
}
