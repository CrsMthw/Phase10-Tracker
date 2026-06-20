package com.crsmthw.phase10tracker.data.db

import androidx.room.*
import com.crsmthw.phase10tracker.data.model.*
import kotlinx.coroutines.flow.Flow

// ── Player DAO ───────────────────────────────────────────────────────────────

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Long): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("UPDATE players SET gamesPlayed = gamesPlayed + 1 WHERE id IN (:ids)")
    suspend fun incrementGamesPlayed(ids: List<Long>)

    @Query("UPDATE players SET gamesWon = gamesWon + 1 WHERE id = :id")
    suspend fun incrementGamesWon(id: Long)

    @Query("UPDATE players SET gamesPlayed = CASE WHEN gamesPlayed > 0 THEN gamesPlayed - 1 ELSE 0 END WHERE id IN (:ids)")
    suspend fun decrementGamesPlayed(ids: List<Long>)

    @Query("UPDATE players SET gamesWon = CASE WHEN gamesWon > 0 THEN gamesWon - 1 ELSE 0 END WHERE id = :id")
    suspend fun decrementGamesWon(id: Long)

    @Query("UPDATE players SET gamesPlayed = 0, gamesWon = 0")
    suspend fun resetAllStats()
}

// ── Game DAO ─────────────────────────────────────────────────────────────────

@Dao
interface GameDao {

    @Query("UPDATE games SET isComplete = 1, finishedAt = :now WHERE isComplete = 0")
    suspend fun cancelAllIncompleteGames(now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM games WHERE isComplete = 0 ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveGame(): GameEntity?

    @Query("SELECT * FROM games WHERE isComplete = 0 ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveGame(): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameById(id: Long): Flow<GameEntity?>

    // The latest finished game with a real winner whose results haven't been shown yet.
    // Lets a completed game still surface its winner screen after a cold launch.
    @Query("""
        SELECT * FROM games
        WHERE isComplete = 1 AND winnerId IS NOT NULL AND winnerId != -1 AND resultsSeen = 0
        ORDER BY finishedAt DESC LIMIT 1
    """)
    fun observePendingResults(): Flow<GameEntity?>

    @Query("UPDATE games SET resultsSeen = 1 WHERE id = :id")
    suspend fun markResultsSeen(id: Long)

    // Finished games with a real winner (excludes cancelled / all-zero games where winnerId = -1).
    @Query("""
        SELECT * FROM games
        WHERE isComplete = 1 AND winnerId IS NOT NULL AND winnerId != -1
        ORDER BY finishedAt DESC
    """)
    fun getFinishedGames(): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("UPDATE games SET currentRound = :round, currentDealerIndex = :dealerIndex WHERE id = :id")
    suspend fun advanceRound(id: Long, round: Int, dealerIndex: Int)

    @Query("""
        UPDATE games
        SET isComplete = 1, finishedAt = :finishedAt, winnerId = :winnerId, resultsSeen = 0
        WHERE id = :id
    """)
    suspend fun finishGame(id: Long, winnerId: Long, finishedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    // Wipes finished games only — keeps any in-progress (isComplete = 0) game.
    @Query("DELETE FROM games WHERE isComplete = 1")
    suspend fun deleteFinishedGames()
}

// ── GamePlayer DAO ───────────────────────────────────────────────────────────

@Dao
interface GamePlayerDao {

    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY turnOrder ASC")
    fun getGamePlayers(gameId: Long): Flow<List<GamePlayerEntity>>

    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY turnOrder ASC")
    suspend fun getGamePlayersList(gameId: Long): List<GamePlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGamePlayers(players: List<GamePlayerEntity>)

    @Update
    suspend fun updateGamePlayer(player: GamePlayerEntity)

    @Update
    suspend fun updateGamePlayers(players: List<GamePlayerEntity>)

    @Query("SELECT * FROM game_players WHERE id = :id")
    suspend fun getGamePlayerById(id: Long): GamePlayerEntity?

    // Relabel a player's denormalized name across all games (used when deleting them from the roster).
    @Query("UPDATE game_players SET playerName = :name WHERE playerId = :playerId")
    suspend fun relabelPlayer(playerId: Long, name: String)
}

// ── CustomPhaseSet DAO ───────────────────────────────────────────────────────

@Dao
interface CustomPhaseSetDao {

    @Query("SELECT * FROM custom_phase_sets ORDER BY createdAt DESC")
    fun getAllPhaseSets(): Flow<List<CustomPhaseSetEntity>>

    @Query("SELECT * FROM custom_phase_sets WHERE id = :id")
    suspend fun getPhaseSetById(id: Long): CustomPhaseSetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhaseSet(phaseSet: CustomPhaseSetEntity): Long

    @Delete
    suspend fun deletePhaseSet(phaseSet: CustomPhaseSetEntity)
}

@Dao
interface RoundDao {

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber ASC")
    fun getRoundsForGame(gameId: Long): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber ASC")
    suspend fun getRoundsForGameList(gameId: Long): List<RoundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRounds(rounds: List<RoundEntity>)

    @Update
    suspend fun updateRounds(rounds: List<RoundEntity>)

    @Query("SELECT * FROM rounds WHERE gamePlayerId = :gamePlayerId ORDER BY roundNumber ASC")
    suspend fun getRoundsForPlayer(gamePlayerId: Long): List<RoundEntity>
}
