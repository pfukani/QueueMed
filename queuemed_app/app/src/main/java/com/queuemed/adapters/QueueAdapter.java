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
    private List<PatientQueueItem> queueList;
    private String userEmailKey; // current patient
    private OnPatientClickListener listener;

    // 🔹 Constructor for patients (no click actions)
    public QueueAdapter(Context context, List<PatientQueueItem> queueList, String userEmailKey) {
        this.context = context;
        this.queueList = queueList;
        this.userEmailKey = userEmailKey;
        this.listener = null;
    }

    // 🔹 Constructor for staff (clickable items)
    public QueueAdapter(Context context, List<PatientQueueItem> queueList, String userEmailKey, OnPatientClickListener listener) {
        this.context = context;
        this.queueList = queueList;
        this.userEmailKey = userEmailKey;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_queue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PatientQueueItem item = queueList.get(position);

        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(item.getPatientName());
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "Waiting");

        // Highlight current user
        if (item.getPatientEmail() != null && item.getPatientEmail().replace(".", "_").equals(userEmailKey)) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.teal_200));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        // Handle clicks only if listener is provided
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onPatientClick(item));
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvQueuePosition);
            tvName = itemView.findViewById(R.id.tvQueueName);
            tvStatus = itemView.findViewById(R.id.tvQueueStatus);
        }
    }

    // 🔹 Interface for staff click actions
    public interface OnPatientClickListener {
        void onPatientClick(PatientQueueItem patient);
    }
}
