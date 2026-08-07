package com.emmikal.securedoselog

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DrugEntry::class, Session::class, SessionEntryCrossRef::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
    abstract fun sessionDao(): SessionDao
}