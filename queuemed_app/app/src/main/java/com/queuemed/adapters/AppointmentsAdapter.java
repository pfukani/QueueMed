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
import com.queuemed.models.Appointment;

import java.util.List;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private CheckInCallback callback;

    // Constructor with callback
    public AppointmentsAdapter(Context context, List<Appointment> appointments, CheckInCallback callback) {
        this.context = context;
        this.appointments = appointments;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvTime.setText("Time: " + appointment.getTime());
        holder.tvStatus.setText("Status: " + appointment.getStatus());

        holder.btnCheckIn.setOnClickListener(v -> {
            if (callback != null) {
                callback.onCheckIn(appointment); // trigger check-in in the fragment
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDate, tvTime, tvStatus;
        Button btnCheckIn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }

    // Callback interface
    public interface CheckInCallback {
        void onCheckIn(Appointment appointment);
    }
}
