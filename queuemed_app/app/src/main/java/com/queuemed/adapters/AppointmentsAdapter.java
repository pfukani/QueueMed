package com.queuemed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.queuemed.R;
import com.queuemed.models.Appointment;

import java.util.List;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointmentList;

    public AppointmentsAdapter(Context context, List<Appointment> appointmentList) {
        this.context = context;
        this.appointmentList = appointmentList;
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
        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvTime.setText("Time: " + appointment.getTime());
        holder.tvStatus.setText("Status: " + appointment.getStatus());

        // Show or hide Check-In button
        if ("Pending".equals(appointment.getStatus())) {
            holder.btnCheckIn.setVisibility(View.VISIBLE);
        } else {
            holder.btnCheckIn.setVisibility(View.GONE);
        }

        holder.btnCheckIn.setOnClickListener(v -> {
            // Update status in Firebase
            FirebaseDatabase.getInstance()
                    .getReference("appointments")
                    .child(appointment.getPatientEmail().replace(".", "_"))
                    .child(appointment.getId())
                    .child("status")
                    .setValue("Checked In")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Checked in successfully!", Toast.LENGTH_SHORT).show();
                        appointment.setStatus("Checked In");
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Check-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDate, tvTime, tvStatus;
        Button btnCheckIn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}
