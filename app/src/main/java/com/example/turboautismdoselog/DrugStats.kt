package com.example.turboautismdoselog

data class DrugStats(
    var drug: String? = null,
    var total: Int = 0,
    var lastTimestamp: Long = 0,
    var firstTimestamp: Long = 0
)