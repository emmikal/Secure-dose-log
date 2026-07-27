package com.example.turboautismdoselog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.room.Room;

import com.example.turboautismdoselog.security.DatabaseProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DrugAdapter adapter;
    private AppDatabase db;

    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_export) {
                exportDatabaseToCSV();
                return true;
            }

            if (item.getItemId() == R.id.action_statistics) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.action_import) {
                openCSVPicker();
                return true;
            }

            return false;
        });

        FloatingActionButton fab = findViewById(R.id.fabAddEntry);
        fab.setOnClickListener(v -> openAddEntrySheet());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyState = findViewById(R.id.emptyState);

        db = DatabaseProvider.getDatabase(getApplicationContext());

        refreshList();
        setupSwipeDelete();
    }

    private void refreshList() {

        List<DrugEntry> entries = db.drugDao().getAll();

        if (entries.isEmpty()) {

            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {

            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {

                adapter = new DrugAdapter(entries, entry -> openEditEntrySheet(entry));
                recyclerView.setAdapter(adapter);

            } else {

                adapter.updateEntries(entries);
            }
        }
    }

    private void setupSwipeDelete() {

        ItemTouchHelper.SimpleCallback swipeHandler =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                        int position = viewHolder.getAdapterPosition();

                        List<DrugEntry> entries = db.drugDao().getAll();
                        DrugEntry deletedEntry = entries.get(position);

                        db.drugDao().delete(deletedEntry);
                        refreshList();

                        Snackbar.make(recyclerView, "Entry deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {

                                    db.drugDao().insert(deletedEntry);
                                    refreshList();

                                })
                                .show();
                    }
                };

        new ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView);
    }

    private void openAddEntrySheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_entry, null);
        dialog.setContentView(view);

        AutoCompleteTextView drug = view.findViewById(R.id.editDrugSheet);
        EditText route = view.findViewById(R.id.editRouteSheet);
        EditText dosage = view.findViewById(R.id.editDosageSheet);
        Button save = view.findViewById(R.id.buttonSaveSheet);

        setupDrugAutocomplete(drug);

        save.setOnClickListener(v -> {

            String drugText = drug.getText().toString();
            String routeText = route.getText().toString();
            String dosageText = dosage.getText().toString();

            if (drugText.isEmpty()) {
                Toast.makeText(this, "Drug name required", Toast.LENGTH_SHORT).show();
                return;
            }

            DrugEntry entry = new DrugEntry();
            entry.drug = drugText;
            entry.route = routeText;
            entry.dosage = dosageText;
            entry.timestamp = System.currentTimeMillis();

            db.drugDao().insert(entry);

            refreshList();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openEditEntrySheet(DrugEntry entry) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_entry, null);
        dialog.setContentView(view);

        AutoCompleteTextView drug = view.findViewById(R.id.editDrugSheet);
        EditText route = view.findViewById(R.id.editRouteSheet);
        EditText dosage = view.findViewById(R.id.editDosageSheet);
        Button save = view.findViewById(R.id.buttonSaveSheet);

        setupDrugAutocomplete(drug);

        drug.setText(entry.drug);
        route.setText(entry.route);
        dosage.setText(entry.dosage);

        save.setOnClickListener(v -> {

            entry.drug = drug.getText().toString();
            entry.route = route.getText().toString();
            entry.dosage = dosage.getText().toString();

            db.drugDao().update(entry);

            refreshList();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupDrugAutocomplete(AutoCompleteTextView field) {

        List<DrugEntry> entries = db.drugDao().getAll();
        List<String> drugNames = new ArrayList<>();

        for (DrugEntry entry : entries) {

            if (entry.drug != null && !drugNames.contains(entry.drug)) {
                drugNames.add(entry.drug);
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        drugNames
                );

        field.setAdapter(adapter);
        field.setThreshold(1);
        field.setOnClickListener(v -> field.showDropDown());
    }

    private void exportDatabaseToCSV() {

        List<DrugEntry> entries = db.drugDao().getAll();

        SimpleDateFormat fileDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());

        String timestamp = fileDateFormat.format(new Date());

        File downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File file = new File(downloadsDir, "drug_log_" + timestamp + ".csv");

        try {

            FileWriter writer = new FileWriter(file);

            writer.append("Drug,Route,Dosage,Timestamp\n");

            for (DrugEntry entry : entries) {

                Date date = new Date(entry.timestamp);

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                String formattedTime = sdf.format(date);

                writer.append(entry.drug).append(",");
                writer.append(entry.route).append(",");
                writer.append(entry.dosage).append(",");
                writer.append(formattedTime).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "CSV exported to Downloads", Toast.LENGTH_LONG).show();

        } catch (IOException e) {

            e.printStackTrace();
            Toast.makeText(this, "Export failed", Toast.LENGTH_LONG).show();
        }
    }

    private static final int PICK_CSV_FILE = 1001;

    private void openCSVPicker() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, PICK_CSV_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK && data != null) {

            importCSV(data.getData());
        }
    }

    private void importCSV(Uri uri) {

        try {

            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            boolean firstLine = true;

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            while ((line = reader.readLine()) != null) {

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length < 4) continue;

                DrugEntry entry = new DrugEntry();

                entry.drug = parts[0];
                entry.route = parts[1];
                entry.dosage = parts[2];

                Date date = sdf.parse(parts[3]);
                entry.timestamp = date.getTime();

                db.drugDao().insert(entry);
            }

            reader.close();

            refreshList();

            Toast.makeText(this, "CSV import complete", Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(this, "Import failed", Toast.LENGTH_LONG).show();
        }
    }
}