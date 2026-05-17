package com.crsmthw.phase10tracker.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class Phase10Database_Impl : Phase10Database() {
  private val _playerDao: Lazy<PlayerDao> = lazy {
    PlayerDao_Impl(this)
  }

  private val _gameDao: Lazy<GameDao> = lazy {
    GameDao_Impl(this)
  }

  private val _gamePlayerDao: Lazy<GamePlayerDao> = lazy {
    GamePlayerDao_Impl(this)
  }

  private val _roundDao: Lazy<RoundDao> = lazy {
    RoundDao_Impl(this)
  }

  private val _customPhaseSetDao: Lazy<CustomPhaseSetDao> = lazy {
    CustomPhaseSetDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "bc3517b21292115f0cf175467ed1cdfd", "0ef263cff3e7a9428b68582d11d073fc") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `gamesPlayed` INTEGER NOT NULL, `gamesWon` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `games` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startedAt` INTEGER NOT NULL, `finishedAt` INTEGER, `isComplete` INTEGER NOT NULL, `winnerId` INTEGER, `currentRound` INTEGER NOT NULL, `currentDealerIndex` INTEGER NOT NULL, `phaseSetId` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `game_players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `gameId` INTEGER NOT NULL, `playerId` INTEGER NOT NULL, `playerName` TEXT NOT NULL, `turnOrder` INTEGER NOT NULL, `currentPhase` INTEGER NOT NULL, `totalScore` INTEGER NOT NULL, `isEliminated` INTEGER NOT NULL, FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`playerId`) REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_game_players_gameId` ON `game_players` (`gameId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_game_players_playerId` ON `game_players` (`playerId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `rounds` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `gameId` INTEGER NOT NULL, `gamePlayerId` INTEGER NOT NULL, `roundNumber` INTEGER NOT NULL, `score` INTEGER NOT NULL, `phaseCompleted` INTEGER NOT NULL, `phaseAtRoundStart` INTEGER NOT NULL, FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`gamePlayerId`) REFERENCES `game_players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_gameId` ON `rounds` (`gameId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_gamePlayerId` ON `rounds` (`gamePlayerId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `custom_phase_sets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `rulesJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bc3517b21292115f0cf175467ed1cdfd')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `players`")
        connection.execSQL("DROP TABLE IF EXISTS `games`")
        connection.execSQL("DROP TABLE IF EXISTS `game_players`")
        connection.execSQL("DROP TABLE IF EXISTS `rounds`")
        connection.execSQL("DROP TABLE IF EXISTS `custom_phase_sets`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsPlayers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlayers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("gamesPlayed", TableInfo.Column("gamesPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("gamesWon", TableInfo.Column("gamesWon", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlayers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlayers: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlayers: TableInfo = TableInfo("players", _columnsPlayers, _foreignKeysPlayers, _indicesPlayers)
        val _existingPlayers: TableInfo = read(connection, "players")
        if (!_infoPlayers.equals(_existingPlayers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |players(com.crsmthw.phase10tracker.data.model.PlayerEntity).
              | Expected:
              |""".trimMargin() + _infoPlayers + """
              |
              | Found:
              |""".trimMargin() + _existingPlayers)
        }
        val _columnsGames: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGames.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("finishedAt", TableInfo.Column("finishedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("isComplete", TableInfo.Column("isComplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("winnerId", TableInfo.Column("winnerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("currentRound", TableInfo.Column("currentRound", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("currentDealerIndex", TableInfo.Column("currentDealerIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGames.put("phaseSetId", TableInfo.Column("phaseSetId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGames: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGames: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGames: TableInfo = TableInfo("games", _columnsGames, _foreignKeysGames, _indicesGames)
        val _existingGames: TableInfo = read(connection, "games")
        if (!_infoGames.equals(_existingGames)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |games(com.crsmthw.phase10tracker.data.model.GameEntity).
              | Expected:
              |""".trimMargin() + _infoGames + """
              |
              | Found:
              |""".trimMargin() + _existingGames)
        }
        val _columnsGamePlayers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGamePlayers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("gameId", TableInfo.Column("gameId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("playerId", TableInfo.Column("playerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("playerName", TableInfo.Column("playerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("turnOrder", TableInfo.Column("turnOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("currentPhase", TableInfo.Column("currentPhase", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("totalScore", TableInfo.Column("totalScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGamePlayers.put("isEliminated", TableInfo.Column("isEliminated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGamePlayers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysGamePlayers.add(TableInfo.ForeignKey("games", "CASCADE", "NO ACTION", listOf("gameId"), listOf("id")))
        _foreignKeysGamePlayers.add(TableInfo.ForeignKey("players", "CASCADE", "NO ACTION", listOf("playerId"), listOf("id")))
        val _indicesGamePlayers: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesGamePlayers.add(TableInfo.Index("index_game_players_gameId", false, listOf("gameId"), listOf("ASC")))
        _indicesGamePlayers.add(TableInfo.Index("index_game_players_playerId", false, listOf("playerId"), listOf("ASC")))
        val _infoGamePlayers: TableInfo = TableInfo("game_players", _columnsGamePlayers, _foreignKeysGamePlayers, _indicesGamePlayers)
        val _existingGamePlayers: TableInfo = read(connection, "game_players")
        if (!_infoGamePlayers.equals(_existingGamePlayers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |game_players(com.crsmthw.phase10tracker.data.model.GamePlayerEntity).
              | Expected:
              |""".trimMargin() + _infoGamePlayers + """
              |
              | Found:
              |""".trimMargin() + _existingGamePlayers)
        }
        val _columnsRounds: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRounds.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRounds.put("gameId", TableInfo.Column("gameId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRounds.put("gamePlayerId", TableInfo.Column("gamePlayerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRounds.put("roundNumber", TableInfo.Column("roundNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRounds.put("score", TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRounds.put("phaseCompleted", TableInfo.Column("phaseCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRounds.put("phaseAtRoundStart", TableInfo.Column("phaseAtRoundStart", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRounds: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysRounds.add(TableInfo.ForeignKey("games", "CASCADE", "NO ACTION", listOf("gameId"), listOf("id")))
        _foreignKeysRounds.add(TableInfo.ForeignKey("game_players", "CASCADE", "NO ACTION", listOf("gamePlayerId"), listOf("id")))
        val _indicesRounds: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRounds.add(TableInfo.Index("index_rounds_gameId", false, listOf("gameId"), listOf("ASC")))
        _indicesRounds.add(TableInfo.Index("index_rounds_gamePlayerId", false, listOf("gamePlayerId"), listOf("ASC")))
        val _infoRounds: TableInfo = TableInfo("rounds", _columnsRounds, _foreignKeysRounds, _indicesRounds)
        val _existingRounds: TableInfo = read(connection, "rounds")
        if (!_infoRounds.equals(_existingRounds)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |rounds(com.crsmthw.phase10tracker.data.model.RoundEntity).
              | Expected:
              |""".trimMargin() + _infoRounds + """
              |
              | Found:
              |""".trimMargin() + _existingRounds)
        }
        val _columnsCustomPhaseSets: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCustomPhaseSets.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCustomPhaseSets.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCustomPhaseSets.put("rulesJson", TableInfo.Column("rulesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCustomPhaseSets.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCustomPhaseSets: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCustomPhaseSets: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCustomPhaseSets: TableInfo = TableInfo("custom_phase_sets", _columnsCustomPhaseSets, _foreignKeysCustomPhaseSets, _indicesCustomPhaseSets)
        val _existingCustomPhaseSets: TableInfo = read(connection, "custom_phase_sets")
        if (!_infoCustomPhaseSets.equals(_existingCustomPhaseSets)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |custom_phase_sets(com.crsmthw.phase10tracker.data.model.CustomPhaseSetEntity).
              | Expected:
              |""".trimMargin() + _infoCustomPhaseSets + """
              |
              | Found:
              |""".trimMargin() + _existingCustomPhaseSets)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "players", "games", "game_players", "rounds", "custom_phase_sets")
  }

  public override fun clearAllTables() {
    super.performClear(true, "players", "games", "game_players", "rounds", "custom_phase_sets")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(PlayerDao::class, PlayerDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GameDao::class, GameDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GamePlayerDao::class, GamePlayerDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RoundDao::class, RoundDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CustomPhaseSetDao::class, CustomPhaseSetDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun playerDao(): PlayerDao = _playerDao.value

  public override fun gameDao(): GameDao = _gameDao.value

  public override fun gamePlayerDao(): GamePlayerDao = _gamePlayerDao.value

  public override fun roundDao(): RoundDao = _roundDao.value

  public override fun customPhaseSetDao(): CustomPhaseSetDao = _customPhaseSetDao.value
}
