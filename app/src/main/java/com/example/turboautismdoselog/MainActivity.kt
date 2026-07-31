package com.example.turboautismdoselog

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turboautismdoselog.security.DatabaseProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.turboautismdoselog.security.disableCopyCut

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var adapter: DrugAdapter? = null
    private lateinit var db: AppDatabase
    private lateinit var emptyState: View

    private lateinit var activeSessionBanner: MaterialCardView
    private lateinit var activeSessionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export -> {
                    exportDatabaseToCSV()
                    true
                }
                R.id.action_statistics -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                R.id.action_import -> {
                    openCSVPicker()
                    true
                }
                R.id.action_start_session -> {
                    showStartSessionDialog()
                    true
                }
                R.id.action_sessions -> {
                    startActivity(Intent(this, SessionsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val fab: FloatingActionButton = findViewById(R.id.fabAddEntry)
        fab.setOnClickListener { openAddEntrySheet() }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        emptyState = findViewById(R.id.emptyState)

        activeSessionBanner = findViewById(R.id.activeSessionBanner)
        activeSessionText = findViewById(R.id.activeSessionText)
        activeSessionBanner.setOnClickListener { onActiveSessionBannerClicked() }

        db = DatabaseProvider.getDatabase(applicationContext)

        refreshList()
        refreshSessionBanner()
        setupSwipeDelete()
    }

    override fun onResume() {
        super.onResume()
        refreshSessionBanner()
    }

    private fun refreshList() {
        val entries = db.drugDao().getAll()

        if (entries.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            val currentAdapter = adapter
            if (currentAdapter == null) {
                adapter = DrugAdapter(entries) { entry -> openEditEntrySheet(entry) }
                recyclerView.adapter = adapter
            } else {
                currentAdapter.updateEntries(entries)
            }
        }
    }

    // ---------- Sessions ----------

    private fun refreshSessionBanner() {
        val activeSessions = db.sessionDao().getActiveSessions()

        if (activeSessions.isEmpty()) {
            activeSessionBanner.visibility = View.GONE
        } else {
            activeSessionBanner.visibility = View.VISIBLE
            activeSessionText.text = if (activeSessions.size == 1) {
                "Active session: ${activeSessions[0].name}"
            } else {
                "Active sessions: ${activeSessions.joinToString(", ") { it.name }}"
            }
        }
    }

    private fun defaultSessionName(): String {
        val sdf = SimpleDateFormat("EEEE HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun showStartSessionDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_start_session, null)
        dialog.setContentView(view)

        val nameField: EditText = view.findViewById(R.id.editSessionName)
        val startButton: Button = view.findViewById(R.id.buttonStartSession)

        nameField.disableCopyCut()

        startButton.setOnClickListener {
            val typedName = nameField.text.toString().trim()
            val name = if (typedName.isEmpty()) defaultSessionName() else typedName

            val session = Session(name = name, startTime = System.currentTimeMillis())
            db.sessionDao().insertSession(session)

            refreshSessionBanner()
            dialog.dismiss()

            Toast.makeText(this, "Session started: $name", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun onActiveSessionBannerClicked() {
        val activeSessions = db.sessionDao().getActiveSessions()

        if (activeSessions.isEmpty()) return

        if (activeSessions.size == 1) {
            endSession(activeSessions[0])
            return
        }

        val names = activeSessions.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("End which session?")
            .setItems(names) { _, which ->
                endSession(activeSessions[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun endSession(session: Session) {
        session.endTime = System.currentTimeMillis()
        db.sessionDao().updateSession(session)
        refreshSessionBanner()
        Toast.makeText(this, "Session ended: ${session.name}", Toast.LENGTH_SHORT).show()
    }

    private fun assignEntryToActiveSessions(entryId: Long) {
        val activeSessions = db.sessionDao().getActiveSessions()

        if (activeSessions.isEmpty()) return

        if (activeSessions.size == 1) {
            db.sessionDao().insertCrossRef(
                SessionEntryCrossRef(sessionId = activeSessions[0].id, entryId = entryId.toInt())
            )
            return
        }

        val names = activeSessions.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(activeSessions.size)

        AlertDialog.Builder(this)
            .setTitle("Assign to session(s)")
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Assign") { _, _ ->
                for (i in activeSessions.indices) {
                    if (checkedItems[i]) {
                        db.sessionDao().insertCrossRef(
                            SessionEntryCrossRef(
                                sessionId = activeSessions[i].id,
                                entryId = entryId.toInt()
                            )
                        )
                    }
                }
            }
            .setNegativeButton("Skip", null)
            .show()
    }

    // ---------- Swipe to delete ----------

    private fun setupSwipeDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val entries = db.drugDao().getAll()
                val deletedEntry = entries[position]

                db.drugDao().delete(deletedEntry)
                refreshList()

                Snackbar.make(recyclerView, "Entry deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        db.drugDao().insert(deletedEntry)
                        refreshList()
                    }
                    .show()
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    // ---------- Add / edit entry ----------

    private fun openAddEntrySheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_entry, null)
        dialog.setContentView(view)

        val drug: AutoCompleteTextView = view.findViewById(R.id.editDrugSheet)
        val route: EditText = view.findViewById(R.id.editRouteSheet)
        val dosage: EditText = view.findViewById(R.id.editDosageSheet)
        val save: Button = view.findViewById(R.id.buttonSaveSheet)

        drug.disableCopyCut()
        route.disableCopyCut()
        dosage.disableCopyCut()

        setupDrugAutocomplete(drug)

        save.setOnClickListener {
            val drugText = drug.text.toString()
            val routeText = route.text.toString()
            val dosageText = dosage.text.toString()

            if (drugText.isEmpty()) {
                Toast.makeText(this, "Drug name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val entry = DrugEntry()
            entry.drug = drugText
            entry.route = routeText
            entry.dosage = dosageText
            entry.timestamp = System.currentTimeMillis()

            val newEntryId = db.drugDao().insert(entry)

            refreshList()
            dialog.dismiss()

            assignEntryToActiveSessions(newEntryId)
        }

        dialog.show()
    }

    private fun openEditEntrySheet(entry: DrugEntry) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_entry, null)
        dialog.setContentView(view)

        val drug: AutoCompleteTextView = view.findViewById(R.id.editDrugSheet)
        val route: EditText = view.findViewById(R.id.editRouteSheet)
        val dosage: EditText = view.findViewById(R.id.editDosageSheet)
        val save: Button = view.findViewById(R.id.buttonSaveSheet)

        drug.disableCopyCut()
        route.disableCopyCut()
        dosage.disableCopyCut()

        setupDrugAutocomplete(drug)

        drug.setText(entry.drug)
        route.setText(entry.route)
        dosage.setText(entry.dosage)

        save.setOnClickListener {
            entry.drug = drug.text.toString()
            entry.route = route.text.toString()
            entry.dosage = dosage.text.toString()

            db.drugDao().update(entry)

            refreshList()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupDrugAutocomplete(field: AutoCompleteTextView) {
        val entries = db.drugDao().getAll()
        val drugNames = mutableListOf<String>()

        for (entry in entries) {
            val name = entry.drug
            if (name != null && !drugNames.contains(name)) {
                drugNames.add(name)
            }
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            drugNames
        )

        field.setAdapter(adapter)
        field.threshold = 1
        field.setOnClickListener { field.showDropDown() }
    }

    // ---------- CSV export / import ----------

    private fun exportDatabaseToCSV() {
        val entries = db.drugDao().getAll()

        val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val timestamp = fileDateFormat.format(Date())

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val file = File(downloadsDir, "drug_log_$timestamp.csv")

        try {
            val writer = FileWriter(file)
            writer.append("Drug,Route,Dosage,Timestamp\n")

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            for (entry in entries) {
                val date = Date(entry.timestamp)
                val formattedTime = sdf.format(date)

                writer.append(entry.drug).append(",")
                writer.append(entry.route).append(",")
                writer.append(entry.dosage).append(",")
                writer.append(formattedTime).append("\n")
            }

            writer.flush()
            writer.close()

            Toast.makeText(this, "CSV exported to Downloads", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Export failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun openCSVPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "text/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        startActivityForResult(intent, PICK_CSV_FILE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { importCSV(it) }
        }
    }

    private fun importCSV(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var firstLine = true
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (firstLine) {
                    firstLine = false
                    continue
                }

                val parts = line!!.split(",")
                if (parts.size < 4) continue

                val entry = DrugEntry()
                entry.drug = parts[0]
                entry.route = parts[1]
                entry.dosage = parts[2]

                val date = sdf.parse(parts[3])
                entry.timestamp = date?.time ?: continue

                db.drugDao().insert(entry)
            }

            reader.close()
            refreshList()

            Toast.makeText(this, "CSV import complete", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Import failed", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val PICK_CSV_FILE = 1001
    }
}