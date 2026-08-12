package com.emmikal.securedoselog

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.emmikal.securedoselog.security.DatabaseProvider
import com.emmikal.securedoselog.substances.EffectsEstimator
import com.emmikal.securedoselog.substances.SubstanceDatabase
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionDetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { finish() }

        db = DatabaseProvider.getDatabase(applicationContext)

        val sessionId = intent.getIntExtra("sessionId", -1)
        if (sessionId == -1) {
            finish()
            return
        }

        loadSessionDetail(sessionId)
    }

    private fun loadSessionDetail(sessionId: Int) {
        val session = db.sessionDao().getSessionById(sessionId) ?: run {
            finish()
            return
        }

        val entries = db.sessionDao().getEntriesForSession(sessionId)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        findViewById<TextView>(R.id.detailSessionName).text = session.name

        findViewById<TextView>(R.id.detailStarted).text =
            getString(R.string.started, sdf.format(Date(session.startTime)))

        val endTime = session.endTime
        val endedText = if (endTime != null) {
            getString(R.string.ended, sdf.format(Date(endTime)))
        } else {
            getString(R.string.ended_still_active)
        }

        findViewById<TextView>(R.id.detailEnded).text = endedText

        val durationMillis =
            (endTime ?: System.currentTimeMillis()) - session.startTime

        findViewById<TextView>(R.id.detailDuration).text =
            getString(R.string.duration, formatDuration(durationMillis))

        findViewById<TextView>(R.id.detailEntryCount).text =
            getString(R.string.entries_count, entries.size)

        // Group entries by substance, preserving first-seen order
        val bySubstance = LinkedHashMap<String, MutableList<DrugEntry>>()

        for (entry in entries) {
            val drug = entry.drug ?: getString(R.string.unknown)
            bySubstance.getOrPut(drug) { mutableListOf() }.add(entry)
        }

        val inflater = LayoutInflater.from(this)

        val substanceContainer =
            findViewById<android.widget.LinearLayout>(R.id.substanceContainer)

        for ((drug, drugEntries) in bySubstance) {
            val itemView = inflater.inflate(
                R.layout.item_substance_summary,
                substanceContainer,
                false
            )

            itemView.findViewById<TextView>(R.id.substanceName).text = drug

            val doses = drugEntries.map { it.dosage ?: "" }

            itemView.findViewById<TextView>(R.id.substanceDoses).text =
                doses
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .ifEmpty {
                        getString(R.string.doses_count, doses.size)
                    }

            val redoseNote =
                itemView.findViewById<TextView>(R.id.substanceRedoseNote)

            if (drugEntries.size > 1) {
                val redoseText = buildRedoseNote(drugEntries)

                if (redoseText != null) {
                    redoseNote.visibility = android.view.View.VISIBLE
                    redoseNote.text = redoseText
                }
            }

            substanceContainer.addView(itemView)
        }

        // Timeline: chronological list of every entry
        val timelineContainer =
            findViewById<android.widget.LinearLayout>(R.id.timelineContainer)

        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val sortedEntries = entries.sortedBy { it.timestamp }

        for (entry in sortedEntries) {
            val itemView = inflater.inflate(
                R.layout.item_timeline_entry,
                timelineContainer,
                false
            )

            itemView.findViewById<TextView>(R.id.timelineTime).text =
                timeSdf.format(Date(entry.timestamp))

            val dosage = entry.dosage?.takeIf { it.isNotBlank() }
            val drug = entry.drug ?: getString(R.string.unknown)

            itemView.findViewById<TextView>(R.id.timelineDescription).text =
                if (dosage != null) {
                    "$dosage $drug"
                } else {
                    drug
                }

            timelineContainer.addView(itemView)
        }
    }

    /**
     * Builds a caveat note for a substance logged more than once in
     * a session. Does not attempt to calculate a combined end time --
     * only surfaces the most recent linked dose's own estimate,
     * with a note that actual combined effects likely last longer.
     */
    private fun buildRedoseNote(drugEntries: List<DrugEntry>): String? {
        val linkedEntries = drugEntries.filter {
            it.substanceId != null && it.linkedRoute != null
        }

        if (linkedEntries.isEmpty()) return null

        val mostRecent =
            linkedEntries.maxByOrNull { it.timestamp } ?: return null

        val estimate = EffectsEstimator.formatEstimate(
            this@SessionDetailActivity,
            mostRecent
        ) ?: return null

        val substanceName =
            SubstanceDatabase.findById(mostRecent.substanceId!!)?.name
                ?: mostRecent.drug
                ?: getString(R.string.this_substance)

        return getString(
            R.string.redose_note,
            drugEntries.size,
            substanceName,
            estimate
        )
    }

    private fun formatDuration(millis: Long): String {
        val totalMinutes = millis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return getString(
            R.string.duration_format,
            hours,
            minutes
        )
    }
}