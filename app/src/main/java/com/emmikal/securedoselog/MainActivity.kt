package com.emmikal.securedoselog

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emmikal.securedoselog.security.DatabaseProvider
import com.emmikal.securedoselog.security.disableCopyCut
import com.emmikal.securedoselog.substances.ActiveSubstanceFinder
import com.emmikal.securedoselog.substances.Interaction
import com.emmikal.securedoselog.substances.InteractionEngine
import com.emmikal.securedoselog.substances.InteractionSeverity
import com.emmikal.securedoselog.substances.SubstanceDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.joinToString

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

        SubstanceDatabase.load(applicationContext)


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
                getString(R.string.active_session, activeSessions[0].name)
            } else {
                getString(
                    R.string.active_sessions,
                    activeSessions.joinToString(", ") { it.name }
                )
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

            Toast.makeText(
                this,
                getString(R.string.session_started, name),
                Toast.LENGTH_SHORT
            ).show()
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
            .setTitle(getString(R.string.end_which_session))
            .setItems(names) { _, which ->
                endSession(activeSessions[which])
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun endSession(session: Session) {
        session.endTime = System.currentTimeMillis()
        db.sessionDao().updateSession(session)
        refreshSessionBanner()
        Toast.makeText(
            this,
            getString(R.string.session_ended, session.name),
            Toast.LENGTH_SHORT
        ).show()
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
            .setTitle(R.string.assign_to_sessions)
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(R.string.assign) { _, _ ->
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
            .setNegativeButton(R.string.skip, null)
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

                Snackbar.make(
                    recyclerView,
                    R.string.entry_deleted,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.undo) {
                        db.drugDao().insert(deletedEntry)
                        refreshList()
                    }
                    .show()
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    // ---------- Substance linking ----------

    /**
     * Watches the drug name field and shows/hides the "link to known
     * substance" section based on whether the typed text matches an
     * entry in SubstanceDatabase. When a route is picked from the
     * spinner (anything other than "Don't link"), it overwrites the
     * visible Route field so the choice is reflected in the log.
     */
    private fun setupSubstanceLinking(
        drugField: AutoCompleteTextView,
        linkSection: MaterialCardView,
        linkPrompt: TextView,
        routeSpinner: Spinner,
        routeField: EditText
    ) {
        drugField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val match = SubstanceDatabase.findByName(s?.toString() ?: "")

                if (match == null) {
                    linkSection.visibility = View.GONE
                    return
                }

                linkSection.visibility = View.VISIBLE
                linkPrompt.text = getString(R.string.link_known_substance, match.name)

                val routeOptions = mutableListOf(getString(R.string.dont_link))
                routeOptions.addAll(match.routes.map { it.route.replaceFirstChar { c -> c.uppercase() } })

                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    routeOptions
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                routeSpinner.adapter = spinnerAdapter
            }
        })

        routeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent?.getItemAtPosition(position) as? String ?: return
                if (selected != getString(R.string.dont_link)) {
                    routeField.setText(selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Reads the current spinner selection and, if a route was chosen
     * (not "Don't link"), returns the matching substanceId + route
     * name to store on the entry. Returns null,null if not linked.
     */
    private fun readLinkSelection(
        drugField: AutoCompleteTextView,
        routeSpinner: Spinner
    ): Pair<String?, String?> {
        val match = SubstanceDatabase.findByName(drugField.text.toString()) ?: return null to null
        val selected = routeSpinner.selectedItem as? String ?: return null to null

        if (selected == getString(R.string.dont_link)) return null to null

        return match.id to selected
    }

    // ---------- Add / edit entry ----------

    private fun openAddEntrySheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_entry, null)
        dialog.setContentView(view)

        val drug: AutoCompleteTextView = view.findViewById(R.id.editDrugSheet)
        val route: EditText = view.findViewById(R.id.editRouteSheet)
        val dosage: EditText = view.findViewById(R.id.editDosageSheet)
        val notes: EditText = view.findViewById(R.id.editNotesSheet)
        val save: Button = view.findViewById(R.id.buttonSaveSheet)

        val linkSection: MaterialCardView = view.findViewById(R.id.linkSubstanceSection)
        val linkPrompt: TextView = view.findViewById(R.id.linkPromptText)
        val routeSpinner: Spinner = view.findViewById(R.id.spinnerRoute)

        setupDrugAutocomplete(drug)
        setupSubstanceLinking(drug, linkSection, linkPrompt, routeSpinner, route)

        drug.disableCopyCut()
        route.disableCopyCut()
        dosage.disableCopyCut()
        notes.disableCopyCut()

        save.setOnClickListener {
            val drugText = drug.text.toString()
            val routeText = route.text.toString()
            val dosageText = dosage.text.toString()
            val notesText = notes.text.toString()

            if (drugText.isEmpty()) {
                Toast.makeText(
                    this,
                    R.string.drug_name_required,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val (linkedSubstanceId, linkedRouteName) = readLinkSelection(drug, routeSpinner)

            val entry = DrugEntry()
            entry.drug = drugText.trim()
            entry.route = routeText
            entry.dosage = dosageText
            entry.timestamp = System.currentTimeMillis()
            entry.notes = notesText.ifBlank { null }
            entry.substanceId = linkedSubstanceId
            entry.linkedRoute = linkedRouteName

            attemptSaveEntry(entry, dialog)
        }

        dialog.show()
    }

    private fun saveEntry(
        entry: DrugEntry,
        dialog: BottomSheetDialog
    ) {

        val newEntryId = db.drugDao().insert(entry)

        refreshList()
        dialog.dismiss()

        assignEntryToActiveSessions(newEntryId)
    }

    private fun attemptSaveEntry(
        entry: DrugEntry,
        dialog: BottomSheetDialog
    ) {

        val interactions = findInteractions(entry)

        if (interactions.isEmpty()) {
            saveEntry(entry, dialog)
            return
        }

        showInteractionWarningDialog(
            interactions,
            entry,
            dialog
        )
    }

    private fun deduplicateInteractions(
        interactions: List<Interaction>
    ): List<Interaction> {

        val map = mutableMapOf<String, Interaction>()

        for (interaction in interactions) {

            val key = listOf(
                interaction.existing.id,
                interaction.incoming.id
            )
                .sorted()
                .joinToString("|")

            val existing = map[key]

            if (
                existing == null ||
                interaction.severity.ordinal < existing.severity.ordinal
            ) {
                map[key] = interaction
            }
        }

        return map.values.toList()
    }
    private fun showInteractionWarningDialog(
        interactions: List<Interaction>,
        entry: DrugEntry,
        dialog: BottomSheetDialog
    ) {

        val message = buildString {

            append(getString(R.string.interactions_detected))
            append("\n\n")

            val uniqueInteractions =
                deduplicateInteractions(interactions)

            uniqueInteractions.forEach {

                val icon = when (it.severity) {
                    InteractionSeverity.DANGEROUS -> "🔴"
                    InteractionSeverity.UNSAFE -> "🟠"
                    InteractionSeverity.UNCERTAIN -> "🟡"
                }

                append(icon)
                append(" ")
                append(it.existing.name)
                append(" + ")
                append(it.incoming.name)
                append("\n")

                append(getString(R.string.matched_via))
                append(" ")
                append(it.matchedInteraction)
                append("\n\n")
            }

            append(getString(R.string.potentially_unsafe_combinations))
            append("\n\n")
            append(getString(R.string.log_dose_confirmation))
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.interaction_detected)
            .setMessage(message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.log_anyway) { _, _ ->
                saveEntry(entry, dialog)
            }

            .show()
    }

    private fun findInteractions(
        entry: DrugEntry
    ): List<Interaction> {

        val substanceId = entry.substanceId
            ?: return emptyList()

        val incoming =
            SubstanceDatabase.findById(substanceId)
                ?: return emptyList()

        val existingEntries =
            db.drugDao().getAll()

        val activeSubstances =
            ActiveSubstanceFinder.findActiveSubstances(existingEntries)

        return InteractionEngine.findInteractions(
            existing = activeSubstances.map { it.substance },
            incoming = incoming
        )
    }

    private fun openEditEntrySheet(entry: DrugEntry) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_entry, null)
        dialog.setContentView(view)

        val drug: AutoCompleteTextView = view.findViewById(R.id.editDrugSheet)
        val route: EditText = view.findViewById(R.id.editRouteSheet)
        val dosage: EditText = view.findViewById(R.id.editDosageSheet)
        val notes: EditText = view.findViewById(R.id.editNotesSheet)
        val save: Button = view.findViewById(R.id.buttonSaveSheet)

        val linkSection: MaterialCardView = view.findViewById(R.id.linkSubstanceSection)
        val linkPrompt: TextView = view.findViewById(R.id.linkPromptText)
        val routeSpinner: Spinner = view.findViewById(R.id.spinnerRoute)

        setupDrugAutocomplete(drug)
        setupSubstanceLinking(drug, linkSection, linkPrompt, routeSpinner, route)

        drug.disableCopyCut()
        route.disableCopyCut()
        dosage.disableCopyCut()
        notes.disableCopyCut()

        drug.setText(entry.drug)
        route.setText(entry.route)
        dosage.setText(entry.dosage)
        notes.setText(entry.notes)

        // If this entry was already linked, pre-select that route once
        // the spinner has been populated by the TextWatcher above.
        val existingLinkedRoute = entry.linkedRoute
        if (existingLinkedRoute != null) {
            routeSpinner.post {
                val spinnerAdapter = routeSpinner.adapter
                if (spinnerAdapter != null) {
                    for (i in 0 until spinnerAdapter.count) {
                        if (spinnerAdapter.getItem(i) == existingLinkedRoute) {
                            routeSpinner.setSelection(i)
                            break
                        }
                    }
                }
            }
        }

        save.setOnClickListener {
            val (linkedSubstanceId, linkedRouteName) = readLinkSelection(drug, routeSpinner)

            entry.drug = drug.text.toString().trim()
            entry.route = route.text.toString()
            entry.dosage = dosage.text.toString()
            entry.notes = notes.text.toString().ifBlank { null }
            entry.substanceId = linkedSubstanceId
            entry.linkedRoute = linkedRouteName

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
        val fileName = "drug_log_$timestamp.csv"

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri == null) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_LONG).show()
            return
        }

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()
                writer.append("Drug,Route,Dosage,Timestamp,Notes\n")

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                for (entry in entries) {
                    val date = Date(entry.timestamp)
                    val formattedTime = sdf.format(date)

                    writer.append(csvEscape(entry.drug)).append(",")
                    writer.append(csvEscape(entry.route)).append(",")
                    writer.append(csvEscape(entry.dosage)).append(",")
                    writer.append(csvEscape(formattedTime)).append(",")
                    writer.append(csvEscape(entry.notes)).append("\n")
                }

                writer.flush()
            }

            Toast.makeText(
                this,
                R.string.csv_exported_to_downloads,
                Toast.LENGTH_LONG
            ).show()

        } catch (e: IOException) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_LONG).show()
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
        var importedCount = 0
        var duplicateCount = 0
        var skippedCount = 0

        val maxImportFileSize = 10L * 1024 * 1024 // 10 MB
        val maxImportRows = 100_000
        val maxFieldLength = 1_000
        val maxLineLength = 10_000

        try {
            val cursor = contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.SIZE),
                null,
                null,
                null
            )

            val fileSize = cursor?.use {
                if (it.moveToFirst() && !it.isNull(0)) {
                    it.getLong(0)
                } else {
                    -1L
                }
            } ?: -1L

            if (fileSize > maxImportFileSize) {
                Toast.makeText(
                    this,
                    R.string.import_file_too_large,
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->

                    var firstLine = true
                    var rowCount = 0

                    val sdf = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).apply {
                        isLenient = false
                    }

                    var line: String?

                    while (reader.readLine().also { line = it } != null) {

                        if (firstLine) {
                            firstLine = false
                            continue
                        }

                        rowCount++

                        if (rowCount > maxImportRows) {
                            skippedCount++
                            continue
                        }

                        val currentLine = line ?: continue

                        if (currentLine.length > maxLineLength) {
                            skippedCount++
                            continue
                        }

                        try {
                            val csvParts = parseCsvLine(currentLine)

                            // A valid exported row contains:
                            // Drug, Route, Dosage, Timestamp, Notes
                            if (csvParts.size < 4 || csvParts.size > 5) {
                                skippedCount++
                                continue
                            }

                            val drug = csvParts[0].trim()
                            val route = csvParts[1].trim()
                            val dosage = csvParts[2].trim()
                            val timestampText = csvParts[3].trim()

                            val notes = if (csvParts.size == 5) {
                                csvParts[4].trim().ifBlank { null }
                            } else {
                                null
                            }

                            // Required fields
                            if (drug.isBlank() ||
                                route.isBlank() ||
                                dosage.isBlank() ||
                                timestampText.isBlank()
                            ) {
                                skippedCount++
                                continue
                            }

                            // Protect against oversized fields
                            if (drug.length > maxFieldLength ||
                                route.length > maxFieldLength ||
                                dosage.length > maxFieldLength ||
                                timestampText.length > maxFieldLength ||
                                (notes?.length ?: 0) > maxFieldLength
                            ) {
                                skippedCount++
                                continue
                            }

                            val date = sdf.parse(timestampText)

                            if (date == null) {
                                skippedCount++
                                continue
                            }

                            val timestamp = date.time

                            val alreadyExists =
                                db.drugDao().countMatching(
                                    drug,
                                    route,
                                    dosage,
                                    timestamp
                                ) > 0

                            if (alreadyExists) {
                                duplicateCount++
                                continue
                            }

                            val entry = DrugEntry().apply {
                                this.drug = drug
                                this.route = route
                                this.dosage = dosage
                                this.timestamp = timestamp
                                this.notes = notes
                            }

                            db.drugDao().insert(entry)
                            importedCount++

                        } catch (e: Exception) {
                            skippedCount++
                        }
                    }
                }
            } ?: run {
                Toast.makeText(
                    this,
                    R.string.import_failed_read,
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            refreshList()

            val resultParts = mutableListOf(
                getString(R.string.imported_entries, importedCount)
            )

            if (duplicateCount > 0) {
                resultParts.add(
                    getString(R.string.duplicates_skipped, duplicateCount)
                )
            }

            if (skippedCount > 0) {
                resultParts.add(
                    getString(R.string.rows_skipped, skippedCount)
                )
            }

            Toast.makeText(
                this,
                resultParts.joinToString(", "),
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                R.string.import_failed_read,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val PICK_CSV_FILE = 1001
    }
}