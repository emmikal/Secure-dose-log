package com.emmikal.securedoselog

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emmikal.securedoselog.substances.EffectsEstimator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrugAdapter(
    private var entries: List<DrugEntry>,
    private val listener: OnItemLongClickListener
) : RecyclerView.Adapter<DrugAdapter.ViewHolder>() {

    fun interface OnItemLongClickListener {
        fun onItemLongClick(entry: DrugEntry)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drug: TextView = view.findViewById(R.id.textDrug)
        val route: TextView = view.findViewById(R.id.textRoute)
        val dosage: TextView = view.findViewById(R.id.textDosage)
        val timestamp: TextView = view.findViewById(R.id.textTimestamp)
        val effectsEstimate: TextView = view.findViewById(R.id.textEffectsEstimate)
        val notes: TextView = view.findViewById(R.id.textNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drug_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]

        holder.drug.text = entry.drug
        holder.route.text = entry.route
        holder.dosage.text = entry.dosage

        val date = Date(entry.timestamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.timestamp.text = sdf.format(date)

        val estimateText = EffectsEstimator.formatEstimate(
            holder.itemView.context,
            entry
        )

        if (estimateText == null) {
            holder.effectsEstimate.visibility = View.GONE
        } else {
            holder.effectsEstimate.visibility = View.VISIBLE
            holder.effectsEstimate.text = estimateText
        }

        val note = entry.notes
        if (note.isNullOrBlank()) {
            holder.notes.visibility = View.GONE
        } else {
            holder.notes.visibility = View.VISIBLE
            holder.notes.text = note
        }

        holder.itemView.setOnClickListener { v ->
            val intent = Intent(v.context, DrugStatisticsActivity::class.java)
            intent.putExtra("drug", entry.drug)
            v.context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(entry)
            true
        }
    }

    override fun getItemCount(): Int = entries.size

    fun updateEntries(newEntries: List<DrugEntry>) {
        this.entries = newEntries
        notifyDataSetChanged()
    }
}