package com.crsmthw.phase10tracker.data.model

// Shown in the active game scoreboard
data class PlayerGameState(
    val gamePlayerId: Long,
    val playerId: Long,
    val playerName: String,
    val turnOrder: Int,
    val currentPhase: Int,
    val totalScore: Int,
    val isDealer: Boolean = false,
    val hasCompletedAllPhases: Boolean = false
)

// Used for the round-entry screen
data class RoundEntry(
    val gamePlayerId: Long,
    val playerName: String,
    val currentPhase: Int,
    val scoreInput: String = "",
    val phaseCompleted: Boolean = false,
    val autoCompleted: Boolean = false   // true when score was auto-inferred < threshold
)

// Custom phase set UI model
data class CustomPhaseSet(
    val id: Long,
    val name: String,
    val phases: List<PhaseRule>
)

// Leaderboard row
data class LeaderboardEntry(
    val playerId: Long,
    val playerName: String,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val winPercentage: Float = if (gamesPlayed > 0) gamesWon.toFloat() / gamesPlayed else 0f
)

// End of game summary row
data class GameResult(
    val playerName: String,
    val finalScore: Int,
    val finalPhase: Int,
    val isWinner: Boolean
)

// One finished-game row in the Game History list
data class GameSummary(
    val gameId: Long,
    val finishedAt: Long,
    val playerNames: List<String>,
    val winnerName: String,
    val winnerScore: Int,
    val isTie: Boolean,
    val phaseSetName: String,
    val roundsPlayed: Int
)

// One editable player line while editing a past round (Round History)
data class RoundEditEntry(
    val roundId: Long,
    val gamePlayerId: Long,
    val playerName: String,
    val phaseAtRoundStart: Int,
    val scoreInput: String,
    val phaseCompleted: Boolean,
    val autoCompleted: Boolean = false
)

// The round currently being edited
data class RoundDraft(
    val roundNumber: Int,
    val entries: List<RoundEditEntry>
)
