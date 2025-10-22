package com.queuemed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.queuemed.R;
import com.queuemed.models.PatientQueueItem;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private Context context;
    private List<PatientQueueItem> patientList;

    public QueueAdapter(Context context, List<PatientQueueItem> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_queue_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PatientQueueItem patient = patientList.get(position);
        holder.tvName.setText(patient.getName());
        holder.tvEmail.setText(patient.getEmail());
        holder.tvCheckInStatus.setText(patient.getCheckInStatus());
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvCheckInStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvCheckInStatus = itemView.findViewById(R.id.tvCheckInStatus);
        }
    }
}
