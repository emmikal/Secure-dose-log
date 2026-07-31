package com.example.turboautismdoselog

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.turboautismdoselog.security.DatabaseProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DrugStatisticsActivity : AppCompatActivity() {

    private lateinit var statDrugName: TextView
    private lateinit var statTotal: TextView
    private lateinit var statToday: TextView
    private lateinit var statWeek: TextView
    private lateinit var statAverage: TextView
    private lateinit var statFirst: TextView
    private lateinit var statLast: TextView

    private lateinit var db: AppDatabase
    private var drug: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drug_statistics)

        statDrugName = findViewById(R.id.statDrugName)
        statTotal = findViewById(R.id.statTotal)
        statToday = findViewById(R.id.statToday)
        statWeek = findViewById(R.id.statWeek)
        statAverage = findViewById(R.id.statAverage)
        statFirst = findViewById(R.id.statFirst)
        statLast = findViewById(R.id.statLast)

        drug = intent.getStringExtra("drug")

        statDrugName.text = drug

        db = DatabaseProvider.getDatabase(applicationContext)

        loadStats()
    }

    private fun loadStats() {
        val currentDrug = drug ?: return
        val entries = db.drugDao().getEntriesForDrug(currentDrug)

        if (entries.isEmpty()) return

        // Total doses
        statTotal.text = entries.size.toString()

        // Start of today (timezone safe)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val startOfDay = cal.timeInMillis

        val today = entries.count { it.timestamp >= startOfDay }
        statToday.text = today.toString()

        // Entries last 7 days
        val weekCal = Calendar.getInstance()
        weekCal.set(Calendar.HOUR_OF_DAY, 0)
        weekCal.set(Calendar.MINUTE, 0)
        weekCal.set(Calendar.SECOND, 0)
        weekCal.set(Calendar.MILLISECOND, 0)
        weekCal.add(Calendar.DAY_OF_YEAR, -7)

        val startOfWeek = weekCal.timeInMillis

        val week = entries.count { it.timestamp >= startOfWeek }
        statWeek.text = week.toString()

        // First and last dose
        val firstTimestamp = entries.minOf { it.timestamp }
        val lastTimestamp = entries.maxOf { it.timestamp }

        val firstDate = Date(firstTimestamp)
        val lastDate = Date(lastTimestamp)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        statFirst.text = sdf.format(firstDate)
        statLast.text = sdf.format(lastDate)

        // Average doses per day
        var days = (lastTimestamp - firstTimestamp) / (1000.0 * 60 * 60 * 24)

        if (days < 1) {
            days = 1.0
        }

        val avg = entries.size / days

        statAverage.text = String.format(Locale.getDefault(), "%.2f", avg)
    }
}