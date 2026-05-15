package com.crsmthw.phase10tracker.data.model

data class PhaseRule(
    val phaseNumber: Int,
    val title: String,
    val description: String
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
