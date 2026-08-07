package com.emmikal.securedoselog

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emmikal.securedoselog.security.DatabaseProvider
import com.google.android.material.appbar.MaterialToolbar

class SessionsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var recyclerSessions: RecyclerView
    private lateinit var emptyState: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerSessions = findViewById(R.id.recyclerSessions)
        recyclerSessions.layoutManager = LinearLayoutManager(this)

        emptyState = findViewById(R.id.emptyState)

        db = DatabaseProvider.getDatabase(applicationContext)

        loadSessions()
    }

    override fun onResume() {
        super.onResume()
        loadSessions()
    }

    private fun loadSessions() {
        val sessions = db.sessionDao().getAllSessions()

        if (sessions.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerSessions.visibility = View.GONE
            return
        }

        emptyState.visibility = View.GONE
        recyclerSessions.visibility = View.VISIBLE

        val entryCounts = sessions.associate { session ->
            session.id to db.sessionDao().getEntriesForSession(session.id).size
        }

        recyclerSessions.adapter = SessionsAdapter(sessions, entryCounts) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("sessionId", session.id)
            startActivity(intent)
        }
    }
}