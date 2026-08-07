package com.emmikal.securedoselog.substances

import com.emmikal.securedoselog.DrugEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Computes an approximate effects-end range for an entry linked to
 * a known substance + route. This is a single-dose estimate only --
 * it does not account for redosing, tolerance, or individual
 * variation. See SubstanceDatabase for data sourcing.
 */
object EffectsEstimator {

    data class EstimatedRange(
        val endMinTimestamp: Long,
        val endMaxTimestamp: Long
    )

    fun estimateFor(entry: DrugEntry): EstimatedRange? {
        val substanceId = entry.substanceId ?: return null
        val routeName = entry.linkedRoute ?: return null

        val substance = SubstanceDatabase.findById(substanceId) ?: return null
        val routeDuration = substance.routes.find {
            it.route.equals(routeName, ignoreCase = true)
        } ?: return null

        val total = routeDuration.total ?: return null
        val minMinutes = total.minMinutes ?: return null
        val maxMinutes = total.maxMinutes ?: return null

        val endMin = entry.timestamp + minMinutes * 60_000L
        val endMax = entry.timestamp + maxMinutes * 60_000L

        return EstimatedRange(endMin, endMax)
    }

    /**
     * Short, list-friendly display string, e.g. "Est. effects end
     * ~14:30-16:00" or "Effects likely ended by 16:00" if the max
     * estimate has already passed.
     */
    fun formatEstimate(entry: DrugEntry): String? {
        val estimate = estimateFor(entry) ?: return null
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()

        val startText = sdf.format(Date(estimate.endMinTimestamp))
        val endText = sdf.format(Date(estimate.endMaxTimestamp))

        return if (now > estimate.endMaxTimestamp) {
            "Effects likely ended by $endText"
        } else {
            "Est. effects end ~$startText\u2013$endText"
        }
    }
}