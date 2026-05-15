package com.crsmthw.phase10tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_rulesets")
data class CustomRulesetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    // Stored as pipe-separated "title|description" per phase, comma-separated phases
    // e.g. "2 Sets of 3|Two groups...,1 Set of 3 + 1 Run of 4|..."
    val rulesData: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Helper to serialize/deserialize rules list to/from the stored string
fun List<PhaseRule>.toRulesData(): String =
    joinToString(",") { "${it.title}|${it.description}" }

fun String.toPhaseRules(): List<PhaseRule> =
    split(",").mapIndexed { index, entry ->
        val parts = entry.split("|")
        PhaseRule(
            phaseNumber = index + 1,
            title = parts.getOrElse(0) { "Phase ${index + 1}" },
            description = parts.getOrElse(1) { "" }
        )
    }

// UI model for ruleset picker
data class RulesetOption(
    val id: Long,           // -1 = official
    val name: String,
    val rules: List<PhaseRule>
) {
    companion object {
        val OFFICIAL = RulesetOption(-1L, "Official Rules", OFFICIAL_PHASE_RULES)
    }
}
