package com.queuemed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.queuemed.R;
import com.queuemed.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface CheckInCallback {
        void onCheckIn(Appointment appointment);
    }

    private Context context;
    private List<Appointment> appointmentList;
    private CheckInCallback checkInCallback;
    private boolean showCheckInButton;
    private boolean isStaffView = false; // Default to patient view

    public AppointmentAdapter(Context context, List<Appointment> appointmentList,
                              CheckInCallback checkInCallback, boolean showCheckInButton) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.checkInCallback = checkInCallback;
        this.showCheckInButton = showCheckInButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // For staff view, show "See Doctor" instead of actual doctor name
        String doctorText = appointment.getDoctorName();
        if (isStaffView && !appointment.getDoctorName().isEmpty()) {
            doctorText = "See Doctor:";
        }

        holder.tvDoctorName.setText(doctorText);
        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvTime.setText("Time: " + appointment.getTime());
        holder.tvStatus.setText("Status: " + appointment.getStatus());

        // For staff, NEVER show check-in button
        // For patients, only show if conditions are met
        boolean shouldShowCheckIn = showCheckInButton &&
                checkInCallback != null &&
                !isStaffView &&
                "Pending".equalsIgnoreCase(appointment.getStatus());

        if (shouldShowCheckIn) {
            holder.btnCheckIn.setVisibility(View.VISIBLE);
            holder.btnCheckIn.setOnClickListener(v -> checkInCallback.onCheckIn(appointment));
        } else {
            holder.btnCheckIn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    // Simple method to set staff view
    public void setStaffView(boolean isStaffView) {
        this.isStaffView = isStaffView;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDate, tvTime, tvStatus;
        Button btnCheckIn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}