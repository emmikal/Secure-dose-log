package com.emmikal.securedoselog

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var name: String = "",
    var startTime: Long = 0,
    var endTime: Long? = null
)