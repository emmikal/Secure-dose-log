package com.example.turboautismdoselog

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SessionDao {

    @Insert
    fun insertSession(session: Session): Long

    @Update
    fun updateSession(session: Session)

    @Delete
    fun deleteSession(session: Session)

    @Query("SELECT * FROM Session WHERE endTime IS NULL ORDER BY startTime DESC")
    fun getActiveSessions(): List<Session>

    @Query("SELECT * FROM Session ORDER BY startTime DESC")
    fun getAllSessions(): List<Session>

    @Insert
    fun insertCrossRef(crossRef: SessionEntryCrossRef)

    @Query(
        "SELECT DrugEntry.* FROM DrugEntry " +
                "INNER JOIN SessionEntryCrossRef ON DrugEntry.id = SessionEntryCrossRef.entryId " +
                "WHERE SessionEntryCrossRef.sessionId = :sessionId " +
                "ORDER BY timestamp ASC"
    )
    fun getEntriesForSession(sessionId: Int): List<DrugEntry>

    @Query("SELECT * FROM Session WHERE id = :sessionId LIMIT 1")
    fun getSessionById(sessionId: Int): Session?
}