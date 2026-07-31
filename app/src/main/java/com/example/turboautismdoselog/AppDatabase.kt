package com.example.turboautismdoselog

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DrugEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
}