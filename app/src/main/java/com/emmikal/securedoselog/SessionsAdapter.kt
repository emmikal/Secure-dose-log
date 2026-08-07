package com.emmikal.securedoselog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionsAdapter(
    private val sessions: List<Session>,
    private val entryCounts: Map<Int, Int>,
    private val listener: OnSessionClickListener
) : RecyclerView.Adapter<SessionsAdapter.ViewHolder>() {

    fun interface OnSessionClickListener {
        fun onSessionClick(session: Session)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.sessionName)
        val status: TextView = view.findViewById(R.id.sessionStatus)
        val entryCount: TextView = view.findViewById(R.id.sessionEntryCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        holder.name.text = session.name

        val end = session.endTime
        holder.status.text = if (end == null) {
            "Active — started ${sdf.format(Date(session.startTime))}"
        } else {
            "Ended ${sdf.format(Date(end))}"
        }

        val count = entryCounts[session.id] ?: 0
        holder.entryCount.text = "$count ${if (count == 1) "entry" else "entries"}"

        holder.itemView.setOnClickListener { listener.onSessionClick(session) }
    }

    override fun getItemCount(): Int = sessions.size
}