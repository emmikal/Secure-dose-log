package com.example.turboautismdoselog.substances

import com.example.turboautismdoselog.DrugEntry

data class ActiveSubstance(
    val entry: DrugEntry,
    val substance: KnownSubstance,
    val route: RouteDuration?,
    val estimatedEnd: Long?
)

object ActiveSubstanceFinder {

    /**
     * Returns all substances that are still possibly active.
     *
     * If no duration information is available, the substance is treated
     * as active (conservative behaviour for harm reduction).
     */
    fun findActiveSubstances(
        entries: List<DrugEntry>,
        now: Long = System.currentTimeMillis()
    ): List<ActiveSubstance> {

        val active = mutableListOf<ActiveSubstance>()

        for (entry in entries) {

            val substanceId = entry.substanceId ?: continue

            val substance =
                SubstanceDatabase.findById(substanceId)
                    ?: continue

            val route = substance.routes.find {
                it.route.equals(
                    entry.linkedRoute,
                    ignoreCase = true
                )
            }

            val estimatedEnd = calculateEstimatedEnd(
                entry,
                route
            )

            if (estimatedEnd == null || estimatedEnd > now) {
                active += ActiveSubstance(
                    entry = entry,
                    substance = substance,
                    route = route,
                    estimatedEnd = estimatedEnd
                )
            }
        }

        return active
    }

    /**
     * Returns true if the logged substance may still be active.
     */
    fun isPossiblyActive(
        entry: DrugEntry,
        now: Long = System.currentTimeMillis()
    ): Boolean {

        val substanceId = entry.substanceId ?: return false

        val substance =
            SubstanceDatabase.findById(substanceId)
                ?: return false

        val route = substance.routes.find {
            it.route == entry.linkedRoute
        }

        val estimatedEnd =
            calculateEstimatedEnd(entry, route)

        return estimatedEnd == null || estimatedEnd > now
    }

    /**
     * Calculates the estimated end of effects using the maximum
     * total duration reported by PsychonautWiki.
     *
     * Returns null if no suitable duration information exists.
     */
    private fun calculateEstimatedEnd(
        entry: DrugEntry,
        route: RouteDuration?
    ): Long? {

        val maxMinutes =
            route?.total?.maxMinutes
                ?: return null

        return entry.timestamp + (maxMinutes * 60_000L)
    }
}