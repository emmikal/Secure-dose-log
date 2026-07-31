package com.example.turboautismdoselog

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DrugDao {

    @Insert
    fun insert(entry: DrugEntry): Long

    @Query("SELECT * FROM DrugEntry ORDER BY timestamp DESC")
    fun getAll(): List<DrugEntry>

    @Delete
    fun delete(entry: DrugEntry)

    @Update
    fun update(entry: DrugEntry)

    @Query(
        "SELECT drug, " +
                "COUNT(*) AS total, " +
                "MIN(timestamp) AS firstTimestamp," +
                "MAX(timestamp) AS lastTimestamp " +
                "FROM DrugEntry " +
                "GROUP BY drug " +
                "ORDER BY total DESC"
    )
    fun getDrugStats(): List<DrugStats>

    @Query("SELECT * FROM DrugEntry WHERE drug = :drug ORDER BY timestamp DESC")
    fun getEntriesForDrug(drug: String): List<DrugEntry>
}