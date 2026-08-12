package com.emmikal.securedoselog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emmikal.securedoselog.security.DatabaseProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var statTotalEntries: TextView
    private lateinit var statMostUsedDrug: TextView
    private lateinit var statEntriesToday: TextView
    private lateinit var statEntriesWeek: TextView
    private lateinit var statAvgPerDay: TextView
    private lateinit var statLastDose: TextView

    private lateinit var recyclerDrugStats: RecyclerView
    private var drugStatsAdapter: DrugStatsAdapter? = null

    private lateinit var emptyState: View
    private lateinit var statisticsContent: View

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        statTotalEntries = findViewById(R.id.statTotalEntries)
        statMostUsedDrug = findViewById(R.id.statMostUsedDrug)
        statEntriesToday = findViewById(R.id.statEntriesToday)
        statEntriesWeek = findViewById(R.id.statEntriesWeek)
        statAvgPerDay = findViewById(R.id.statAvgPerDay)
        statLastDose = findViewById(R.id.statLastDose)

        recyclerDrugStats = findViewById(R.id.recyclerDrugStats)
        recyclerDrugStats.layoutManager = LinearLayoutManager(this)

        emptyState = findViewById(R.id.emptyState)
        statisticsContent = findViewById(R.id.statisticsContent)

        db = DatabaseProvider.getDatabase(applicationContext)

        loadStatistics()
    }

    private fun loadStatistics() {
        val entries = db.drugDao().getAll()

        if (entries.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            statisticsContent.visibility = View.GONE
            return
        } else {
            emptyState.visibility = View.GONE
            statisticsContent.visibility = View.VISIBLE
        }

        // Total entries
        statTotalEntries.text = entries.size.toString()

        // Most used drug
        val stats = db.drugDao().getDrugStats()

        if (stats.isNotEmpty()) {
            statMostUsedDrug.text = stats[0].drug
        }

        // Entries today (timezone safe)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis

        val todayCount = entries.count { it.timestamp >= startOfDay }
        statEntriesToday.text = todayCount.toString()

        // Entries last 7 days (timezone safe)
        val weekCalendar = Calendar.getInstance()
        weekCalendar.set(Calendar.HOUR_OF_DAY, 0)
        weekCalendar.set(Calendar.MINUTE, 0)
        weekCalendar.set(Calendar.SECOND, 0)
        weekCalendar.set(Calendar.MILLISECOND, 0)
        weekCalendar.add(Calendar.DAY_OF_YEAR, -7)

        val weekStart = weekCalendar.timeInMillis

        val weekCount = entries.count { it.timestamp >= weekStart }
        statEntriesWeek.text = weekCount.toString()

        // Average doses per day
        if (entries.size < 2) {
            statAvgPerDay.text = getString(R.string.not_available)
        } else {
            val minTimestamp = entries.minOf { it.timestamp }
            val maxTimestamp = entries.maxOf { it.timestamp }

            val diffMillis = maxTimestamp - minTimestamp
            var days = diffMillis / (1000.0 * 60 * 60 * 24)

            if (days < 1) {
                days = 1.0
            }

            val avg = entries.size / days

            statAvgPerDay.text =
                String.format(Locale.getDefault(), "%.2f", avg)
        }

        // Last dose
        val lastTimestamp = entries.maxOf { it.timestamp }
        val date = Date(lastTimestamp)

        val sdf = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        )

        statLastDose.text = sdf.format(date)

        // Substance statistics RecyclerView
        drugStatsAdapter = DrugStatsAdapter(this, stats)
        recyclerDrugStats.adapter = drugStatsAdapter
    }
}