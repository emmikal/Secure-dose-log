package com.emmikal.securedoselog

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrugStatsAdapter(
    private val context: Context,
    private val stats: List<DrugStats>
) : RecyclerView.Adapter<DrugStatsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drugName: TextView = view.findViewById(R.id.statDrugName)
        val total: TextView = view.findViewById(R.id.statDrugTotal)
        val last: TextView = view.findViewById(R.id.statDrugLast)
        val avg: TextView = view.findViewById(R.id.statDrugAvg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drug_stats, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = stats[position]

        holder.drugName.text = stat.drug
        holder.total.text = context.getString(R.string.total_doses, stat.total)

        val date = Date(stat.lastTimestamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        holder.last.text = context.getString(
            R.string.last_dose,
            sdf.format(date)
        )

        // Calculate average per day
        var days = (stat.lastTimestamp - stat.firstTimestamp) / (1000.0 * 60 * 60 * 24)

        if (days < 1) {
            days = 1.0
        }

        val avg = stat.total / days

        holder.avg.text = context.getString(
            R.string.average_per_day,
            String.format(Locale.getDefault(), "%.2f", avg)
        )
    }

    override fun getItemCount(): Int = stats.size
}