package com.crsmthw.phase10tracker.data.repository

import com.crsmthw.phase10tracker.data.db.*
import com.crsmthw.phase10tracker.data.model.*
import kotlinx.coroutines.flow.*

class GameRepository(
    private val playerDao: PlayerDao,
    private val gameDao: GameDao,
    private val gamePlayerDao: GamePlayerDao,
    private val roundDao: RoundDao,
    private val customRuleSetDao: CustomRuleSetDao
) {

    // ── Players ──────────────────────────────────────────────────────────────

    fun getAllPlayers(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()

    suspend fun addPlayer(name: String): Long =
        playerDao.insertPlayer(PlayerEntity(name = name.trim()))

    suspend fun deletePlayer(player: PlayerEntity) =
        playerDao.deletePlayer(player)

    // ── Active Game Check ────────────────────────────────────────────────────

    suspend fun getActiveGame(): GameEntity? = gameDao.getActiveGame()

    fun observeActiveGame(): Flow<GameEntity?> = gameDao.observeActiveGame()

    // ── Start Game ───────────────────────────────────────────────────────────

    suspend fun startNewGame(orderedPlayers: List<PlayerEntity>): Long {
        // Wipe any lingering incomplete games before starting fresh
        gameDao.cancelAllIncompleteGames()
        val gameId = gameDao.insertGame(GameEntity())
        val gamePlayers = orderedPlayers.mapIndexed { index, player ->
            GamePlayerEntity(
                gameId = gameId,
                playerId = player.id,
                playerName = player.name,
                turnOrder = index,
                currentPhase = 1,
                totalScore = 0
            )
        }
        gamePlayerDao.insertGamePlayers(gamePlayers)
        return gameId
    }

    // ── Live Game State ──────────────────────────────────────────────────────

    fun getGameById(gameId: Long): Flow<GameEntity?> = gameDao.getGameById(gameId)

    fun getGamePlayers(gameId: Long): Flow<List<GamePlayerEntity>> =
        gamePlayerDao.getGamePlayers(gameId)

    fun getActiveBoardState(gameId: Long): Flow<List<PlayerGameState>> =
        combine(
            gameDao.getGameById(gameId),
            gamePlayerDao.getGamePlayers(gameId)
        ) { game, players ->
            val dealerIndex = game?.currentDealerIndex ?: 0
            players.map { gp ->
                PlayerGameState(
                    gamePlayerId = gp.id,
                    playerId = gp.playerId,
                    playerName = gp.playerName,
                    turnOrder = gp.turnOrder,
                    currentPhase = gp.currentPhase,
                    totalScore = gp.totalScore,
                    isDealer = gp.turnOrder == dealerIndex,
                    hasCompletedAllPhases = gp.currentPhase > 10
                )
            }.sortedWith(
                compareByDescending<PlayerGameState> { it.currentPhase }
                    .thenBy { it.totalScore }
            )
        }

    // ── Submit Round ─────────────────────────────────────────────────────────

    suspend fun submitRound(gameId: Long, entries: List<RoundEntry>) {
        val game = gameDao.getActiveGame() ?: return
        val gamePlayers = gamePlayerDao.getGamePlayersList(gameId)

        // Build round records + update player state
        val rounds = entries.mapNotNull { entry ->
            val gp = gamePlayers.find { it.id == entry.gamePlayerId } ?: return@mapNotNull null
            val score = entry.scoreInput.trim().toIntOrNull() ?: 0
            val completed = entry.phaseCompleted
            val newPhase = if (completed && gp.currentPhase <= 10) gp.currentPhase + 1
                           else gp.currentPhase
            val newScore = gp.totalScore + score

            // Update the game player
            gamePlayerDao.updateGamePlayer(
                gp.copy(
                    currentPhase = newPhase,
                    totalScore = newScore
                )
            )

            RoundEntity(
                gameId = gameId,
                gamePlayerId = entry.gamePlayerId,
                roundNumber = game.currentRound,
                score = score,
                phaseCompleted = completed,
                phaseAtRoundStart = gp.currentPhase
            )
        }
        roundDao.insertRounds(rounds)

        // Advance round & dealer
        val playerCount = gamePlayers.size
        val nextDealerIndex = (game.currentDealerIndex + 1) % playerCount
        gameDao.advanceRound(
            id = gameId,
            round = game.currentRound + 1,
            dealerIndex = nextDealerIndex
        )

        // Check win condition — someone advanced past phase 10
        val updatedPlayers = gamePlayerDao.getGamePlayersList(gameId)
        val finishers = updatedPlayers.filter { it.currentPhase > 10 }
        if (finishers.isNotEmpty()) {
            // Highest phase first (all should be > 10 here), then lowest score
            val winnerScore = finishers.minOf { it.totalScore }
            val winners = finishers.filter { it.totalScore == winnerScore }
            // Use first winner for DB (tied winners both get stats updated)
            endGame(gameId, winners.map { it.playerId })
        }
    }

    // ── End Game ─────────────────────────────────────────────────────────────

    suspend fun endGame(gameId: Long, winnerIds: List<Long>? = null) {
        val gamePlayers = gamePlayerDao.getGamePlayersList(gameId)

        val resolvedWinnerIds: List<Long> = if (winnerIds != null) {
            winnerIds
        } else {
            // Early end: highest phase wins, then lowest score, then tie
            val highestPhase = gamePlayers.maxOf { it.currentPhase }
            val topPlayers = gamePlayers.filter { it.currentPhase == highestPhase }
            val lowestScore = topPlayers.minOf { it.totalScore }
            topPlayers.filter { it.totalScore == lowestScore }.map { it.playerId }
        }

        if (resolvedWinnerIds.isEmpty()) return

        // Store first winner in game record (UI will show all tied winners)
        gameDao.finishGame(gameId, resolvedWinnerIds.first())

        // Update lifetime stats for all players and all winners
        val playerIds = gamePlayers.map { it.playerId }
        playerDao.incrementGamesPlayed(playerIds)
        resolvedWinnerIds.forEach { playerDao.incrementGamesWon(it) }
    }

    // ── Results ──────────────────────────────────────────────────────────────

    suspend fun getGameResults(gameId: Long): List<GameResult> {
        val gamePlayers = gamePlayerDao.getGamePlayersList(gameId)

        // Get winner IDs from the finished game record
        val finishedGame = gameDao.getGameById(gameId).firstOrNull()
        val primaryWinnerId = finishedGame?.winnerId

        // Determine all tied winners: same highest phase + same lowest score
        val highestPhase = gamePlayers.maxOf { it.currentPhase }
        val topPlayers = gamePlayers.filter { it.currentPhase == highestPhase }
        val lowestScore = topPlayers.minOf { it.totalScore }
        val winnerIds = topPlayers
            .filter { it.totalScore == lowestScore }
            .map { it.playerId }
            .toSet()

        // Sort: highest phase first, then lowest score (ascending)
        return gamePlayers
            .sortedWith(compareByDescending<GamePlayerEntity> { it.currentPhase }
                .thenBy { it.totalScore })
            .map { gp ->
                GameResult(
                    playerName = gp.playerName,
                    finalScore = gp.totalScore,
                    finalPhase = minOf(gp.currentPhase, 10),
                    isWinner = gp.playerId in winnerIds
                )
            }
    }

    // ── Leaderboard ──────────────────────────────────────────────────────────

    fun getLeaderboard(): Flow<List<LeaderboardEntry>> =
        playerDao.getAllPlayers().map { players ->
            players
                .map { p ->
                    LeaderboardEntry(
                        playerId = p.id,
                        playerName = p.name,
                        gamesPlayed = p.gamesPlayed,
                        gamesWon = p.gamesWon
                    )
                }
                .sortedByDescending { it.winPercentage }
        }

    // ── Cancel game (no winner, no stats update) ─────────────────────────────

    suspend fun cancelGame(gameId: Long) {
        // Mark as complete but with no winner — no stats updated
        gameDao.finishGame(gameId, winnerId = -1L)
    }

    // ── Custom Rule Sets ──────────────────────────────────────────────────────

    fun getAllCustomRuleSets(): Flow<List<CustomRuleSet>> =
        customRuleSetDao.getAllRuleSets().map { entities ->
            entities.map { entity ->
                CustomRuleSet(
                    id = entity.id,
                    name = entity.name,
                    phases = parseRulesJson(entity.rulesJson)
                )
            }
        }

    suspend fun saveCustomRuleSet(name: String, phases: List<PhaseRule>): Long {
        val json = buildRulesJson(phases)
        return customRuleSetDao.insertRuleSet(
            CustomRuleSetEntity(name = name.trim(), rulesJson = json)
        )
    }

    suspend fun deleteCustomRuleSet(ruleSet: CustomRuleSet) {
        customRuleSetDao.deleteRuleSet(
            CustomRuleSetEntity(id = ruleSet.id, name = ruleSet.name, rulesJson = "")
        )
    }

    // Simple JSON helpers (no external library needed for this flat structure)
    private fun buildRulesJson(phases: List<PhaseRule>): String {
        return phases.joinToString(",", "[", "]") { phase ->
            """{"n":${phase.phaseNumber},"t":"${phase.title.replace("\"", "'")}","d":"${phase.description.replace("\"", "'")}"}"""
        }
    }

    private fun parseRulesJson(json: String): List<PhaseRule> {
        return try {
            val items = json.trim('[', ']').split("},")
            items.mapIndexed { index, item ->
                val clean = item.trim().trimEnd('}')
                val n = Regex(""""n":(\d+)""").find(clean)?.groupValues?.get(1)?.toIntOrNull() ?: (index + 1)
                val t = Regex(""""t":"([^"]+)"""").find(clean)?.groupValues?.get(1) ?: "Phase ${index + 1}"
                val d = Regex(""""d":"([^"]+)"""").find(clean)?.groupValues?.get(1) ?: t
                PhaseRule(n, t, d)
            }
        } catch (e: Exception) {
            OFFICIAL_PHASE_RULES
        }
    }
}
