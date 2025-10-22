package com.queuemed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.queuemed.R;
import com.queuemed.models.PatientQueueItem;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    public interface CheckInCallback {
        void onCheckIn(PatientQueueItem patient);
    }

    private Context context;
    private List<PatientQueueItem> patients;
    private CheckInCallback callback;

    public QueueAdapter(Context context, List<PatientQueueItem> patients, CheckInCallback callback) {
        this.context = context;
        this.patients = patients;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_queue_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PatientQueueItem patient = patients.get(position);
        holder.tvName.setText(patient.getName());
        holder.tvStatus.setText(patient.getStatus());

        // Button visibility depends on status
        holder.btnCheckIn.setVisibility(patient.getStatus().equals("Pending") ? View.VISIBLE : View.GONE);

        holder.btnCheckIn.setOnClickListener(v -> {
            if (callback != null) callback.onCheckIn(patient);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        Button btnCheckIn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPatientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}
