package com.example.securedoselog

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DrugEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var drug: String? = null,
    var route: String? = null,
    var dosage: String? = null,
    var timestamp: Long = 0,
    var notes: String? = null,
    var substanceId: String? = null,
    var linkedRoute: String? = null
)