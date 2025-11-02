package com.queuemed.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.queuemed.R;
import com.queuemed.models.ReportItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.VH> {

    private final List<ReportItem> reportList;

    // Constructor
    public ReportsAdapter(List<ReportItem> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ReportItem it = reportList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

        holder.tvName.setText(it.getPatientName() != null ? it.getPatientName() : "Unknown");
        holder.tvStatus.setText(it.getStatus() != null ? it.getStatus() : "N/A");
        holder.tvDate.setText(it.getTimestamp() != 0 ? sdf.format(new Date(it.getTimestamp())) : "No Date");

        holder.tvDetails.setText(
                "BP: " + safe(it.getBloodPressure()) + "\n" +
                        "Temp: " + safe(it.getTemperature()) + "°C\n" +
                        "Pulse: " + safe(it.getPulse()) + " bpm\n" +
                        "Notes: " + safe(it.getNotes())
        );
    }

    private String safe(String value) {
        return value != null ? value : "--";
    }

    @Override
    public int getItemCount() {
        return reportList != null ? reportList.size() : 0;
    }

    // ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvDate, tvDetails;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}