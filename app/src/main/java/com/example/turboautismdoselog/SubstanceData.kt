package com.example.turboautismdoselog.substances

/**
 * Duration data for one route of administration of a substance.
 * Total duration only (not broken into onset/peak/offset phases),
 * expressed as a min-max range in minutes, since individual
 * variation is significant and a single number would overstate
 * precision.
 *
 * Source: PsychonautWiki (https://psychonautwiki.org), content
 * licensed under CC BY-SA 4.0.
 */
data class RouteDuration(
    val route: String,
    val totalMinMinutes: Int,
    val totalMaxMinutes: Int
)

/**
 * A substance with known duration data, manually curated from
 * PsychonautWiki and shipped with the app. Grows incrementally
 * across releases -- there is no automatic or live lookup.
 */
data class KnownSubstance(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val routes: List<RouteDuration>
)

/**
 * Static, offline substance duration reference data.
 *
 * All data is sourced from PsychonautWiki (https://psychonautwiki.org),
 * licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0/).
 * This app is not affiliated with or endorsed by PsychonautWiki.
 *
 * Duration figures are approximate and vary significantly by dose,
 * individual physiology, tolerance, and other factors. This data is
 * for harm-reduction reference only and is not medical advice.
 *
 * When adding or updating a substance, check its "Total" duration
 * range on the substance's /Summary page on PsychonautWiki and
 * update the source comment with the date checked.
 */
object SubstanceDatabase {

    val substances: List<KnownSubstance> = listOf(

        // Source: https://psychonautwiki.org/wiki/Caffeine/Summary
        // Verified: 2026-08-01
        KnownSubstance(
            id = "caffeine",
            name = "Caffeine",
            routes = listOf(
                RouteDuration("Oral", totalMinMinutes = 120, totalMaxMinutes = 300)
            )
        ),

        // Source: https://psychonautwiki.org/wiki/Alcohol/Summary
        // Verified: 2026-08-01
        // Note: figure is per standard dose (~1 drink), not a full
        // session -- repeated drinks extend total duration well
        // beyond this range.
        KnownSubstance(
            id = "alcohol",
            name = "Alcohol",
            aliases = listOf("Ethanol", "Beer", "Wine"),
            routes = listOf(
                RouteDuration("Oral", totalMinMinutes = 90, totalMaxMinutes = 300)
            )
        ),

        // Source: https://psychonautwiki.org/wiki/LSD/Summary
        // Verified: 2026-08-01
        KnownSubstance(
            id = "lsd",
            name = "LSD",
            aliases = listOf("Acid", "Lysergic acid diethylamide"),
            routes = listOf(
                RouteDuration("Oral", totalMinMinutes = 480, totalMaxMinutes = 720)
            )
        ),

        // Source: https://psychonautwiki.org/wiki/Cannabis/Summary
        // Verified: 2026-08-01
        KnownSubstance(
            id = "cannabis",
            name = "Cannabis",
            aliases = listOf("Weed", "Marijuana", "THC"),
            routes = listOf(
                RouteDuration("Smoked", totalMinMinutes = 150, totalMaxMinutes = 300),
                RouteDuration("Oral", totalMinMinutes = 240, totalMaxMinutes = 600)
            )
        ),

        // Source: https://psychonautwiki.org/wiki/MDMA/Summary
        // Verified: 2026-08-01
        KnownSubstance(
            id = "mdma",
            name = "MDMA",
            aliases = listOf("Molly", "Ecstasy"),
            routes = listOf(
                RouteDuration("Oral", totalMinMinutes = 180, totalMaxMinutes = 360)
            )
        )
    )

    fun findById(id: String): KnownSubstance? = substances.find { it.id == id }

    fun findByName(query: String): KnownSubstance? {
        val normalized = query.trim().lowercase()
        return substances.find { substance ->
            substance.name.lowercase() == normalized ||
                    substance.aliases.any { it.lowercase() == normalized }
        }
    }
}