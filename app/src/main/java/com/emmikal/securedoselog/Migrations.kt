package com.example.securedoselog

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE DrugEntry ADD COLUMN notes TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE DrugEntry ADD COLUMN substanceId TEXT")
        db.execSQL("ALTER TABLE DrugEntry ADD COLUMN linkedRoute TEXT")
    }
}