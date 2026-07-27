package com.example.turboautismdoselog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.turboautismdoselog.security.DatabaseProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView statTotalEntries;
    private TextView statMostUsedDrug;
    private TextView statEntriesToday;
    private TextView statEntriesWeek;
    private TextView statAvgPerDay;
    private TextView statLastDose;

    private RecyclerView recyclerDrugStats;
    private DrugStatsAdapter drugStatsAdapter;

    private View emptyState;
    private View statisticsContent;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        statTotalEntries = findViewById(R.id.statTotalEntries);
        statMostUsedDrug = findViewById(R.id.statMostUsedDrug);
        statEntriesToday = findViewById(R.id.statEntriesToday);
        statEntriesWeek = findViewById(R.id.statEntriesWeek);
        statAvgPerDay = findViewById(R.id.statAvgPerDay);
        statLastDose = findViewById(R.id.statLastDose);

        recyclerDrugStats = findViewById(R.id.recyclerDrugStats);
        recyclerDrugStats.setLayoutManager(new LinearLayoutManager(this));

        emptyState = findViewById(R.id.emptyState);
        statisticsContent = findViewById(R.id.statisticsContent);

        db = DatabaseProvider.getDatabase(getApplicationContext());

        loadStatistics();
    }

    private void loadStatistics() {

        List<DrugEntry> entries = db.drugDao().getAll();

        if (entries.isEmpty()) {

            emptyState.setVisibility(View.VISIBLE);
            statisticsContent.setVisibility(View.GONE);
            return;

        } else {

            emptyState.setVisibility(View.GONE);
            statisticsContent.setVisibility(View.VISIBLE);
        }

        // Total entries

        statTotalEntries.setText(String.valueOf(entries.size()));

        // Most used drug

        List<DrugStats> stats = db.drugDao().getDrugStats();

        if (!stats.isEmpty()) {
            statMostUsedDrug.setText(stats.get(0).drug);
        }

        // Entries today (timezone safe)

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startOfDay = calendar.getTimeInMillis();

        int todayCount = 0;

        for (DrugEntry entry : entries) {
            if (entry.timestamp >= startOfDay) {
                todayCount++;
            }
        }

        statEntriesToday.setText(String.valueOf(todayCount));

        // Entries last 7 days (timezone safe)

        Calendar weekCalendar = Calendar.getInstance();

        weekCalendar.set(Calendar.HOUR_OF_DAY, 0);
        weekCalendar.set(Calendar.MINUTE, 0);
        weekCalendar.set(Calendar.SECOND, 0);
        weekCalendar.set(Calendar.MILLISECOND, 0);

        weekCalendar.add(Calendar.DAY_OF_YEAR, -7);

        long weekStart = weekCalendar.getTimeInMillis();

        int weekCount = 0;

        for (DrugEntry entry : entries) {
            if (entry.timestamp >= weekStart) {
                weekCount++;
            }
        }

        statEntriesWeek.setText(String.valueOf(weekCount));

        // Average doses per day

        if (entries.size() < 2) {

            statAvgPerDay.setText("—");

        } else {

            long minTimestamp = Long.MAX_VALUE;
            long maxTimestamp = Long.MIN_VALUE;

            for (DrugEntry entry : entries) {

                if (entry.timestamp < minTimestamp) {
                    minTimestamp = entry.timestamp;
                }

                if (entry.timestamp > maxTimestamp) {
                    maxTimestamp = entry.timestamp;
                }
            }

            long diffMillis = maxTimestamp - minTimestamp;

            double days = diffMillis / (1000.0 * 60 * 60 * 24);

            if (days < 1) {
                days = 1;
            }

            double avg = entries.size() / days;

            statAvgPerDay.setText(
                    String.format(Locale.getDefault(), "%.2f", avg)
            );
        }

        // Last dose

        long lastTimestamp = 0;

        for (DrugEntry entry : entries) {
            if (entry.timestamp > lastTimestamp) {
                lastTimestamp = entry.timestamp;
            }
        }

        Date date = new Date(lastTimestamp);

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        statLastDose.setText(sdf.format(date));

        // Substance statistics RecyclerView

        drugStatsAdapter = new DrugStatsAdapter(stats);
        recyclerDrugStats.setAdapter(drugStatsAdapter);
    }
}