package com.example.turboautismdoselog;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.ViewHolder> {

    private List<DrugEntry> entries;
    private OnItemLongClickListener listener;

    public interface OnItemLongClickListener {
        void onItemLongClick(DrugEntry entry);
    }

    public DrugAdapter(List<DrugEntry> entries, OnItemLongClickListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView drug;
        TextView route;
        TextView dosage;
        TextView timestamp;

        public ViewHolder(View view) {
            super(view);

            drug = view.findViewById(R.id.textDrug);
            route = view.findViewById(R.id.textRoute);
            dosage = view.findViewById(R.id.textDosage);
            timestamp = view.findViewById(R.id.textTimestamp);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drug_entry, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DrugEntry entry = entries.get(position);

        holder.drug.setText(entry.getDrug());
        holder.route.setText(entry.getRoute());
        holder.dosage.setText(entry.getDosage());

        Date date = new Date(entry.getTimestamp());

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        holder.timestamp.setText(sdf.format(date));

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), DrugStatisticsActivity.class);
            intent.putExtra("drug", entry.getDrug());
            v.getContext().startActivity(intent);

        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(entry);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateEntries(List<DrugEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }
}