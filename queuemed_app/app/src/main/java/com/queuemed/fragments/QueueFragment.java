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
import com.queuemed.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class QueueFragment extends Fragment {

    private RecyclerView recyclerView;
    private QueueAdapter adapter;
    private List<PatientQueueItem> queueList = new ArrayList<>();
    private SharedPrefManager sp;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_queue, container, false);

        recyclerView = view.findViewById(R.id.recyclerQueue);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sp = new SharedPrefManager(requireContext());
        String loggedInEmail = sp.getUserEmail();

        // Implement click listener
        QueueAdapter.OnPatientClickListener listener = patient -> {
            // Open PatientDetailsFragment
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, PatientDetailsFragment.newInstance(
                            patient.getPatientName(),
                            patient.getPatientEmail()
                    ))

                    .addToBackStack(null)
                    .commit();
        };

        adapter = new QueueAdapter(getContext(), queueList, loggedInEmail, listener);
        recyclerView.setAdapter(adapter);

        loadQueue();

        return view;
    }

    private void loadQueue() {
        dbRef = FirebaseDatabase.getInstance().getReference("queue");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                queueList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PatientQueueItem item = ds.getValue(PatientQueueItem.class);
                    if (item != null && "Checked In".equalsIgnoreCase(item.getStatus())) {
                        queueList.add(item);
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
