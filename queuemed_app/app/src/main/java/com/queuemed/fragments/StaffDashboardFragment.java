package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.adapters.QueueAdapter;
import com.queuemed.models.PatientQueueItem;

import java.util.ArrayList;
import java.util.List;

public class StaffDashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private QueueAdapter adapter;
    private List<PatientQueueItem> patientList;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff_dashboard, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewQueue);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        patientList = new ArrayList<>();
        adapter = new QueueAdapter(getContext(), patientList);
        recyclerView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference("appointments");

        loadQueue();

        return view;
    }

    private void loadQueue() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot apptSnapshot : userSnapshot.getChildren()) {
                        String name = apptSnapshot.child("patientName").getValue(String.class);
                        String email = apptSnapshot.child("patientEmail").getValue(String.class);
                        String status = apptSnapshot.child("status").getValue(String.class);

                        if (name != null && email != null && status != null) {
                            patientList.add(new PatientQueueItem(name, email, status));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load queue", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
