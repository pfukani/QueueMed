package com.queuemed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.queuemed.R;
import com.queuemed.models.Appointment;

import java.util.List;
import java.util.function.Consumer;

public class CheckInAdapter extends RecyclerView.Adapter<CheckInAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private Consumer<Appointment> onCheckInClick;

    public CheckInAdapter(Context context, List<Appointment> appointments, Consumer<Appointment> onCheckInClick) {
        this.context = context;
        this.appointments = appointments;
        this.onCheckInClick = onCheckInClick;
    }

    @NonNull
    @Override
    public CheckInAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_check_in, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckInAdapter.ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvDate.setText(appointment.getDate());
        holder.tvTime.setText(appointment.getTime());
        holder.tvStatus.setText(appointment.getStatus());

        // Only show "Check-In" button if status is Pending
        if ("Pending".equals(appointment.getStatus())) {
            holder.btnCheckIn.setVisibility(View.VISIBLE);
            holder.btnCheckIn.setOnClickListener(v -> {
                if (onCheckInClick != null) {
                    onCheckInClick.accept(appointment);
                }
            });
        } else {
            holder.btnCheckIn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDate, tvTime, tvStatus;
        MaterialButton btnCheckIn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}
