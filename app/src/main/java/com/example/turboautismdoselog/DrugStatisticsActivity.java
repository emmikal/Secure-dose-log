package com.example.turboautismdoselog;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.turboautismdoselog.security.DatabaseProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DrugStatisticsActivity extends AppCompatActivity {

    private TextView statDrugName;
    private TextView statTotal;
    private TextView statToday;
    private TextView statWeek;
    private TextView statAverage;

    private AppDatabase db;
    private String drug;
    private TextView statFirst;
    private TextView statLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_statistics);

        statDrugName = findViewById(R.id.statDrugName);
        statTotal = findViewById(R.id.statTotal);
        statToday = findViewById(R.id.statToday);
        statWeek = findViewById(R.id.statWeek);
        statAverage = findViewById(R.id.statAverage);
        statFirst = findViewById(R.id.statFirst);
        statLast = findViewById(R.id.statLast);

        drug = getIntent().getStringExtra("drug");

        statDrugName.setText(drug);

        db = DatabaseProvider.getDatabase(getApplicationContext());

        loadStats();
    }

    private void loadStats() {

        List<DrugEntry> entries = db.drugDao().getEntriesForDrug(drug);

        if (entries.isEmpty()) return;

        // Total doses

        statTotal.setText(String.valueOf(entries.size()));

        // Start of today (timezone safe)

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long startOfDay = cal.getTimeInMillis();

        int today = 0;

        for (DrugEntry e : entries) {
            if (e.timestamp >= startOfDay) {
                today++;
            }
        }

        statToday.setText(String.valueOf(today));

        // Entries last 7 days

        Calendar weekCal = Calendar.getInstance();
        weekCal.set(Calendar.HOUR_OF_DAY, 0);
        weekCal.set(Calendar.MINUTE, 0);
        weekCal.set(Calendar.SECOND, 0);
        weekCal.set(Calendar.MILLISECOND, 0);
        weekCal.add(Calendar.DAY_OF_YEAR, -7);

        long startOfWeek = weekCal.getTimeInMillis();

        int week = 0;

        for (DrugEntry e : entries) {
            if (e.timestamp >= startOfWeek) {
                week++;
            }
        }

        statWeek.setText(String.valueOf(week));

        // First and last dose

        long firstTimestamp = Long.MAX_VALUE;
        long lastTimestamp = 0;

        for (DrugEntry e : entries) {

            if (e.timestamp < firstTimestamp) {
                firstTimestamp = e.timestamp;
            }

            if (e.timestamp > lastTimestamp) {
                lastTimestamp = e.timestamp;
            }
        }

        Date firstDate = new Date(firstTimestamp);
        Date lastDate = new Date(lastTimestamp);

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        statFirst.setText(sdf.format(firstDate));
        statLast.setText(sdf.format(lastDate));

        // Average doses per day

        double days = (lastTimestamp - firstTimestamp) / (1000.0 * 60 * 60 * 24);

        if (days < 1) {
            days = 1;
        }

        double avg = entries.size() / days;

        statAverage.setText(
                String.format(Locale.getDefault(), "%.2f", avg)
        );
    }
}