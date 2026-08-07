package com.example.securedoselog.substances

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DurationRange(
    val minMinutes: Int?,
    val maxMinutes: Int?
)

data class RouteDuration(
    val route: String,
    val onset: DurationRange?,
    val comeup: DurationRange?,
    val peak: DurationRange?,
    val offset: DurationRange?,
    val total: DurationRange?,
    val afterglow: DurationRange?
)

data class KnownSubstance(
    val id: String,
    val name: String,
    val aliases: List<String>,

    val systematicName: String?,

    val chemicalClasses: List<String>,
    val psychoactiveClasses: List<String>,

    val dangerousInteractions: List<String>,
    val unsafeInteractions: List<String>,
    val uncertainInteractions: List<String>,

    val routes: List<RouteDuration>
)

/**
 * Loads substance reference data from a bundled JSON asset
 * (assets/substances.json), generated offline by
 * tools/update_substances.py from PsychonautWiki's GraphQL API.
 *
 * All data is sourced from PsychonautWiki (https://psychonautwiki.org),
 * licensed under CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0/).
 * See assets/substances_ATTRIBUTION.txt for full attribution.
 * This app is not affiliated with or endorsed by PsychonautWiki.
 *
 * No network access is ever used to load this data -- the JSON file
 * is bundled at build time and only updated via new app releases.
 */
object SubstanceDatabase {

    @Volatile
    private var substances: List<KnownSubstance> = emptyList()

    @Volatile
    private var loaded = false

    /**
     * Loads and parses the bundled JSON asset. Safe to call multiple
     * times -- only parses once. Call this early (e.g. from
     * Application.onCreate) before any lookups are needed.
     */
    @Synchronized
    fun load(context: Context) {
        if (loaded) return

        try {
            val json = context.assets.open("substances.json")
                .bufferedReader()
                .use { it.readText() }

            substances = parseSubstances(json)
        } catch (e: Exception) {
            e.printStackTrace()
            substances = emptyList()
        }

        loaded = true
    }

    private fun parseStringArray(array: JSONArray?): List<String> {
        if (array == null) return emptyList()

        val result = mutableListOf<String>()

        for (i in 0 until array.length()) {
            result.add(array.getString(i))
        }

        return result
    }

    private fun parseSubstances(json: String): List<KnownSubstance> {
        val array = JSONArray(json)
        val result = mutableListOf<KnownSubstance>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val aliases =
                parseStringArray(obj.optJSONArray("aliases"))

            val chemicalClasses =
                parseStringArray(obj.optJSONArray("chemicalClasses"))

            val psychoactiveClasses =
                parseStringArray(obj.optJSONArray("psychoactiveClasses"))

            val dangerousInteractions =
                parseStringArray(obj.optJSONArray("dangerousInteractions"))

            val unsafeInteractions =
                parseStringArray(obj.optJSONArray("unsafeInteractions"))

            val uncertainInteractions =
                parseStringArray(obj.optJSONArray("uncertainInteractions"))

            val routes = mutableListOf<RouteDuration>()
            val routesArray = obj.optJSONArray("routes")

            if (routesArray != null) {
                for (j in 0 until routesArray.length()) {
                    val routeObj = routesArray.getJSONObject(j)

                    routes.add(
                        RouteDuration(
                            route = routeObj.getString("route"),
                            onset = parseDurationRange(routeObj.optJSONObject("onset")),
                            comeup = parseDurationRange(routeObj.optJSONObject("comeup")),
                            peak = parseDurationRange(routeObj.optJSONObject("peak")),
                            offset = parseDurationRange(routeObj.optJSONObject("offset")),
                            total = parseDurationRange(routeObj.optJSONObject("total")),
                            afterglow = parseDurationRange(routeObj.optJSONObject("afterglow"))
                        )
                    )
                }
            }

            result.add(
                KnownSubstance(
                    id = obj.getString("id"),
                    name = obj.getString("name"),

                    aliases = aliases,
                    systematicName =
                        if (obj.isNull("systematicName"))
                            null
                        else
                            obj.getString("systematicName"),

                    chemicalClasses = chemicalClasses,
                    psychoactiveClasses = psychoactiveClasses,

                    dangerousInteractions = dangerousInteractions,
                    unsafeInteractions = unsafeInteractions,
                    uncertainInteractions = uncertainInteractions,

                    routes = routes
                )
            )
        }

        return result
    }

    private fun parseDurationRange(obj: JSONObject?): DurationRange? {
        if (obj == null) return null

        val min =
            if (obj.isNull("minMinutes"))
                null
            else
                obj.optInt("minMinutes")

        val max =
            if (obj.isNull("maxMinutes"))
                null
            else
                obj.optInt("maxMinutes")

        if (min == null && max == null) return null

        return DurationRange(min, max)
    }

    fun findById(id: String): KnownSubstance? =
        substances.find { it.id == id }

    fun findByName(query: String): KnownSubstance? {
        val normalized = query.trim().lowercase()

        return substances.find {
            it.name.lowercase() == normalized ||

                    it.aliases.any { alias ->
                        alias.lowercase() == normalized
                    } ||

                    it.systematicName?.lowercase() == normalized
        }
    }
}