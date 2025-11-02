package com.queuemed.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.queuemed.R;
import com.queuemed.models.DailySummary;

import java.util.List;

public class DailySummaryAdapter extends RecyclerView.Adapter<DailySummaryAdapter.VH> {

    private final List<DailySummary> summaryList;

    public DailySummaryAdapter(List<DailySummary> summaryList) {
        this.summaryList = summaryList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_summary, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DailySummary summary = summaryList.get(position);
        holder.tvDate.setText(summary.getDate());
        holder.tvTotal.setText("Appointments: " + summary.getTotalAppointments());
        holder.tvCheckedIn.setText("Checked In: " + summary.getCheckedIn());
        holder.tvVitals.setText("Vitals Taken: " + summary.getVitalsTaken());
    }

    @Override
    public int getItemCount() {
        return summaryList != null ? summaryList.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvTotal, tvCheckedIn, tvVitals;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvCheckedIn = itemView.findViewById(R.id.tvCheckedIn);
            tvVitals = itemView.findViewById(R.id.tvVitals);
        }
    }
}