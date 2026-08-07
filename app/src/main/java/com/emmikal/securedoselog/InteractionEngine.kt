package com.example.securedoselog.substances

enum class InteractionSeverity {
    DANGEROUS,
    UNSAFE,
    UNCERTAIN
}

enum class InteractionMatchType {
    SUBSTANCE,
    CHEMICAL_CLASS,
    PSYCHOACTIVE_CLASS
}

data class Interaction(
    val existing: KnownSubstance,
    val incoming: KnownSubstance,
    val severity: InteractionSeverity,
    val matchType: InteractionMatchType,
    val matchedInteraction: String
)

object InteractionEngine {

    private val synonyms = mapOf(
        "depressants" to "depressant",
        "stimulants" to "stimulant",
        "psychedelics" to "psychedelic",

        "amphetamines" to "amphetamine",

        "ssris" to "ssri",
        "snris" to "snri",
        "maois" to "maoi",

        "nbomes" to "nbome",

        "5-meo-xxt" to "5-meo-xxt",
        "5-meo-xxts" to "5-meo-xxt"
    )

    fun findInteractions(
        existing: List<KnownSubstance>,
        incoming: KnownSubstance
    ): List<Interaction> {

        val interactions = mutableListOf<Interaction>()

        for (substance in existing) {

            interactions += checkDirection(
                source = substance,
                target = incoming
            )

            interactions += checkDirection(
                source = incoming,
                target = substance
            )
        }

        return interactions
            .distinctBy {
                Triple(
                    it.existing.id,
                    it.incoming.id,
                    it.severity
                )
            }
            .sortedBy {
                it.severity.ordinal
            }
    }

    private fun checkDirection(
        source: KnownSubstance,
        target: KnownSubstance
    ): List<Interaction> {

        if (source.id == target.id) {
            return emptyList()
        }

        val results = mutableListOf<Interaction>()

        val targetIdentifiers = buildIdentifiers(target)

        checkSeverity(
            source,
            target,
            InteractionSeverity.DANGEROUS,
            source.dangerousInteractions,
            targetIdentifiers,
            results
        )

        checkSeverity(
            source,
            target,
            InteractionSeverity.UNSAFE,
            source.unsafeInteractions,
            targetIdentifiers,
            results
        )

        checkSeverity(
            source,
            target,
            InteractionSeverity.UNCERTAIN,
            source.uncertainInteractions,
            targetIdentifiers,
            results
        )

        return results
    }

    private fun checkSeverity(
        source: KnownSubstance,
        target: KnownSubstance,
        severity: InteractionSeverity,
        interactionList: List<String>,
        targetIdentifiers: Set<String>,
        results: MutableList<Interaction>
    ) {

        for (interaction in interactionList) {

            if (interaction.isBlank()) {
                continue
            }

            val normalized = normalize(interaction)

            if (!matches(normalized, targetIdentifiers)) {
                continue
            }

            val matchType = when {

                normalize(target.name) == normalized ->
                    InteractionMatchType.SUBSTANCE

                target.chemicalClasses.any {
                    normalize(it) == normalized
                } ->
                    InteractionMatchType.CHEMICAL_CLASS

                target.psychoactiveClasses.any {
                    normalize(it) == normalized
                } ->
                    InteractionMatchType.PSYCHOACTIVE_CLASS

                else ->
                    InteractionMatchType.SUBSTANCE
            }

            results += Interaction(
                existing = source,
                incoming = target,
                severity = severity,
                matchType = matchType,
                matchedInteraction = interaction
            )
        }
    }

    private fun buildIdentifiers(
        substance: KnownSubstance
    ): Set<String> {

        return buildSet {

            add(normalize(substance.name))

            substance.aliases.forEach {
                add(normalize(it))
            }

            substance.chemicalClasses.forEach {
                add(normalize(it))
            }

            substance.psychoactiveClasses.forEach {
                add(normalize(it))
            }
        }
    }

    private fun matches(
        interaction: String,
        identifiers: Set<String>
    ): Boolean {

        return interaction in identifiers
    }

    private fun normalize(value: String): String {

        val normalized = value
            .trim()
            .lowercase()

        return synonyms[normalized] ?: normalized
    }
}