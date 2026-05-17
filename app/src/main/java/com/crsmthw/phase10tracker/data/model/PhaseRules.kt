package com.crsmthw.phase10tracker.data.model

data class PhaseRule(
    val phaseNumber: Int,
    val title: String,
    val description: String
)

// ── Preset phase set container ─────────────────────────────────────────────────

data class PresetPhaseSet(
    val name: String,
    val phases: List<PhaseRule>
)

/** Converts a list of title strings into numbered PhaseRules (description = title). */
private fun phaseList(vararg titles: String): List<PhaseRule> =
    titles.mapIndexed { i, t -> PhaseRule(i + 1, t, t) }

val PRESET_PHASE_SETS: List<PresetPhaseSet> = listOf(

    PresetPhaseSet("Island Paradise", phaseList(
        "1 run of 7",
        "1 set of 2 + 2 sets of 3",
        "1 run of 6 + 1 set of 2",
        "3 sets of 2 + 1 set of 3",
        "1 set of 3 + 1 run of 6",
        "2 runs of 4",
        "3 cards of one color + 1 set of 4",
        "8 cards of one color",
        "4 cards of one color + 1 set of 5",
        "9 cards of one color"
    )),

    PresetPhaseSet("Cocoa Canyon", phaseList(
        "6 cards of one color",
        "2 sets of 3",
        "1 run of 8",
        "1 set of 3 + 1 run of 5",
        "7 cards of even or odd",
        "1 run of 4",
        "1 set of 5 + 1 run of 4",
        "6 cards of even or odd",
        "1 set of 4 + 1 run of 3",
        "1 set of 5 + 1 run of 5"
    )),

    PresetPhaseSet("Disco Fever", phaseList(
        "8 even or odd cards",
        "9 even or odd cards",
        "1 color run of 3 + 2 sets of 2",
        "7 of one color",
        "1 color run of 5 + 2 sets of 2",
        "Same color even or odd of 3 + same color even or odd of 4",
        "1 color run of 4 + 1 set of 4",
        "1 color run of 4 + 3 sets of 2",
        "1 run of 3 + 2 sets of 3",
        "1 run of 3 + 1 set of 4 + 1 set of 3"
    )),

    PresetPhaseSet("Cupcake Lounge", phaseList(
        "3 of one color + 3 of one color + 4 of one color",
        "1 color run of 3 + 2 sets of 2",
        "1 set of 4 + 1 wild",
        "2 sets of 3",
        "1 run of 7",
        "1 set of 4",
        "2 color even or odd of 4",
        "1 run of 9",
        "1 color run of 5 + 2 sets of 2",
        "1 color run of 6 + 1 set of 2"
    )),

    PresetPhaseSet("Mountain Vista", phaseList(
        "1 run of 3 + 3 sets of 2",
        "1 run of 8",
        "1 run of 9",
        "1 color run of 3 + 1 set of 3",
        "1 set of 2 + 2 sets of 3",
        "1 set of 2 + 1 set of 3 + 1 set of 4",
        "4 of one color + 6 of one color",
        "5 of one color + 5 of one color",
        "1 run of 5 + 1 set of 3 + 1 set of 2",
        "1 run of 3 + 1 set of 4 + 1 set of 3"
    )),

    PresetPhaseSet("Prehistoric Valley", phaseList(
        "1 even or odd of 9",
        "1 even or odd of 10",
        "1 run of 8",
        "1 run of 10",
        "2 sets of 3",
        "2 sets of 4",
        "1 color run of 4",
        "1 color run of 3 + 3 of one color",
        "1 set of 3 + 1 run of 4",
        "1 set of 4 + 1 run of 6"
    )),

    PresetPhaseSet("Moonlight Drive-In", phaseList(
        "1 set of 4 + 2 sets of 2",
        "2 sets of 3 + 3 of one color",
        "1 run of 7",
        "1 run of 8",
        "1 set of 2 + 2 sets of 3",
        "1 set of 5",
        "1 run of 9",
        "1 run of 6 + 2 sets of 2",
        "1 run of 8 + 1 set of 2",
        "1 set of 4 + 1 run of 6"
    )),

    PresetPhaseSet("Ancient Greece", phaseList(
        "1 set of 2 + 1 run of 6",
        "1 even or odd of 9",
        "1 even or odd of 10",
        "1 color run of 3 + 1 set of 3",
        "1 set of 3 + 1 run of 5",
        "1 set of 5 + 1 run of 4",
        "1 color run of 5",
        "1 color even or odd of 3 + 1 color even or odd of 5",
        "5 sets of 2",
        "2 sets of 3 + 2 sets of 2"
    )),

    PresetPhaseSet("Jazz Club", phaseList(
        "1 even or odd of 5",
        "1 color run of 3 + 1 set of 3",
        "1 set of 2 + 1 run of 4",
        "1 color run of 4",
        "1 run of 4",
        "1 set of 4 + 1 run of 3",
        "1 color even or odd of 5",
        "1 color run of 5 + 1 set of 2",
        "1 color even or odd of 6",
        "1 color run of 5 + 3 of one color"
    )),

    PresetPhaseSet("Vintage Gas Station", phaseList(
        "1 set of 3 + 1 run of 5",
        "1 run of 4 + 1 set of 3 + 1 set of 2",
        "1 run of 3 + 1 set of 3 + 2 sets of 2",
        "1 color run of 4",
        "1 color run of 4 + 1 set of 2",
        "1 color run of 4 + 2 sets of 2",
        "1 set of 5 + 1 run of 4",
        "1 color even or odd of 5",
        "1 color even or odd of 6",
        "1 color run of 3 + 3 of one color + 1 set of 2"
    )),

    PresetPhaseSet("Ocean Reef", phaseList(
        "1 run of 7",
        "1 set of 4 + 1 set of 3",
        "1 color run of 5 + 1 set of 2",
        "1 even or odd of 10",
        "2 sets of 5",
        "3 sets of 2",
        "1 color run of 3 + 1 set of 3",
        "1 color even or odd of 3 + 1 color even or odd of 4",
        "1 run of 7 + 1 set of 2",
        "1 run of 6"
    )),

    PresetPhaseSet("Candy Castle", phaseList(
        "3 sets of 2",
        "1 run of 5 + 1 set of 2",
        "1 set of 3 + 1 run of 4",
        "Even or odd of 7",
        "1 run of 3 + 1 set of 2",
        "1 run of 7",
        "1 set of 3 + 1 run of 5",
        "1 run of 8",
        "2 sets of 4",
        "2 runs of 3"
    )),

    PresetPhaseSet("The Empire Strikes Back", phaseList(
        "3 sets of 3",
        "1 run of 7 + 1 set of 2",
        "4 even/odd of same color + 2 sets of 2",
        "1 set of 4 + 1 set of 3 + 1 set of 2",
        "2 sets of 5",
        "1 run of 9",
        "1 color run of 5 + 1 set of 3",
        "1 color run of 4 + 1 run of 4",
        "5 even/odd of same color + 1 set of 4",
        "3 sets of 3 + a skip card"
    )),

    PresetPhaseSet("Sets Gone Wild", phaseList(
        "1 set of 4 + 1 run of 5",
        "2 sets of 4",
        "3 sets of 3 + a wild card",
        "1 set of 4 + 1 color run of 6",
        "2 sets of 5",
        "1 set of 5 + 1 color run of 5",
        "1 set of 6 + 1 color run of 4",
        "1 set of 4 + 1 color run of 3 + even or odd of 3",
        "2 sets of 4 + 1 set of 2",
        "1 color run of 10"
    )),
)

val OFFICIAL_PHASE_RULES = listOf(
    PhaseRule(1,  "2 sets of 3",              "Two groups of 3 cards with the same number."),
    PhaseRule(2,  "1 set of 3 + 1 run of 4",  "One group of 3 same-number cards, plus 4 consecutive numbers."),
    PhaseRule(3,  "1 set of 4 + 1 run of 4",  "One group of 4 same-number cards, plus 4 consecutive numbers."),
    PhaseRule(4,  "1 run of 7",               "Seven consecutive numbers in any combination of colors."),
    PhaseRule(5,  "1 run of 8",               "Eight consecutive numbers in any combination of colors."),
    PhaseRule(6,  "1 run of 9",               "Nine consecutive numbers in any combination of colors."),
    PhaseRule(7,  "2 sets of 4",              "Two groups of 4 cards with the same number."),
    PhaseRule(8,  "7 cards of 1 color",       "Seven cards all of the same color."),
    PhaseRule(9,  "1 set of 5 + 1 set of 2",  "One group of 5 same-number cards, plus a pair."),
    PhaseRule(10, "1 set of 5 + 1 set of 3",  "One group of 5 same-number cards, plus a group of 3.")
)

fun getPhaseRule(phase: Int, rules: List<PhaseRule> = OFFICIAL_PHASE_RULES): PhaseRule =
    rules.getOrElse(phase - 1) { rules.last() }

const val PHASE_COMPLETE_THRESHOLD = 50
