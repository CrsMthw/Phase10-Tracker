package com.crsmthw.phase10tracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ── Saved player roster ──────────────────────────────────────────────────────

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// ── A single game session ────────────────────────────────────────────────────

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val isComplete: Boolean = false,
    val winnerId: Long? = null,         // references PlayerEntity.id
    val currentRound: Int = 1,
    val currentDealerIndex: Int = 0,    // index into the ordered player list
    // -1 = Official Phases, -2..-15 = preset index, positive = custom phase set DB id
    val phaseSetId: Long = -1L,
    // false once a game finishes until its winner screen has been shown — lets a completed
    // game still surface its results after a cold launch (Home filters out isComplete games).
    val resultsSeen: Boolean = true
)

// ── Per-player state within a game ──────────────────────────────────────────

@Entity(
    tableName = "game_players",
    // Only the game FK cascades. There is intentionally NO foreign key to players: deleting a
    // player must NOT wipe their game history — the denormalized playerName is relabelled to
    // "Deleted Player" instead (see GameRepository.deletePlayer). See MIGRATION_4_5.
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("playerId")]
)
data class GamePlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val playerId: Long,
    val playerName: String,       // denormalized for display after roster changes
    val turnOrder: Int,           // 0-based, determines dealer rotation
    val currentPhase: Int = 1,
    val totalScore: Int = 0,
    val isEliminated: Boolean = false  // completed Phase 10 — still tracked
)

// ── Custom phase sets ────────────────────────────────────────────────────────

@Entity(tableName = "custom_phase_sets")
data class CustomPhaseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rulesJson: String,  // JSON array of PhaseRule serialized
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GamePlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["gamePlayerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("gamePlayerId")]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val gamePlayerId: Long,
    val roundNumber: Int,
    val score: Int,
    val phaseCompleted: Boolean,
    val phaseAtRoundStart: Int
)
