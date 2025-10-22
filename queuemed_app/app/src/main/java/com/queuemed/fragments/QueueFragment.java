package com.queuemed.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.queuemed.R;
import com.queuemed.adapters.QueueAdapter;
import com.queuemed.models.PatientQueueItem;

import java.util.ArrayList;
import java.util.List;

public class QueueFragment extends Fragment {

    private RecyclerView recyclerView;
    private QueueAdapter adapter;
    private List<PatientQueueItem> patientList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        recyclerView = view.findViewById(R.id.recyclerQueue);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        patientList = new ArrayList<>();
        adapter = new QueueAdapter(getContext(), patientList, patient -> {
            // Check-in logic
            FirebaseDatabase.getInstance()
                    .getReference("queue")
                    .child(patient.getId())
                    .child("status")
                    .setValue("Checked In");
        });
        recyclerView.setAdapter(adapter);

        loadQueueFromFirebase();

        return view;
    }

    private void loadQueueFromFirebase() {
        FirebaseDatabase.getInstance().getReference("queue")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        patientList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PatientQueueItem patient = ds.getValue(PatientQueueItem.class);
                            if (patient != null) patientList.add(patient);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // handle error
                    }
                });
    }
}
