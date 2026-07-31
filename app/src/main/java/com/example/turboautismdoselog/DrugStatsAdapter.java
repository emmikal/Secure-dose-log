package com.example.turboautismdoselog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DrugStatsAdapter extends RecyclerView.Adapter<DrugStatsAdapter.ViewHolder> {

    private List<DrugStats> stats;

    public DrugStatsAdapter(List<DrugStats> stats) {
        this.stats = stats;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView drugName;
        TextView total;
        TextView last;
        TextView avg;

        public ViewHolder(View view) {
            super(view);

            drugName = view.findViewById(R.id.statDrugName);
            total = view.findViewById(R.id.statDrugTotal);
            last = view.findViewById(R.id.statDrugLast);
            avg = view.findViewById(R.id.statDrugAvg);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drug_stats, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DrugStats stat = stats.get(position);

        holder.drugName.setText(stat.getDrug());
        holder.total.setText("Total doses: " + stat.getTotal());

        Date date = new Date(stat.getLastTimestamp());

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        holder.last.setText("Last dose: " + sdf.format(date));

        // Calculate average per day

        double days = (stat.getLastTimestamp() - stat.getFirstTimestamp()) /
                (1000.0 * 60 * 60 * 24);

        if (days < 1) {
            days = 1;
        }

        double avg = stat.getTotal() / days;

        holder.avg.setText(
                "Average/day: " +
                        String.format(Locale.getDefault(), "%.2f", avg)
        );
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }
}