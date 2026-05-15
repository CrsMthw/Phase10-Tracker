package com.crsmthw.phase10tracker.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.crsmthw.phase10tracker.`data`.model.GamePlayerEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GamePlayerDao_Impl(
  __db: RoomDatabase,
) : GamePlayerDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGamePlayerEntity: EntityInsertAdapter<GamePlayerEntity>

  private val __updateAdapterOfGamePlayerEntity: EntityDeleteOrUpdateAdapter<GamePlayerEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfGamePlayerEntity = object : EntityInsertAdapter<GamePlayerEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `game_players` (`id`,`gameId`,`playerId`,`playerName`,`turnOrder`,`currentPhase`,`totalScore`,`isEliminated`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GamePlayerEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.gameId)
        statement.bindLong(3, entity.playerId)
        statement.bindText(4, entity.playerName)
        statement.bindLong(5, entity.turnOrder.toLong())
        statement.bindLong(6, entity.currentPhase.toLong())
        statement.bindLong(7, entity.totalScore.toLong())
        val _tmp: Int = if (entity.isEliminated) 1 else 0
        statement.bindLong(8, _tmp.toLong())
      }
    }
    this.__updateAdapterOfGamePlayerEntity = object : EntityDeleteOrUpdateAdapter<GamePlayerEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `game_players` SET `id` = ?,`gameId` = ?,`playerId` = ?,`playerName` = ?,`turnOrder` = ?,`currentPhase` = ?,`totalScore` = ?,`isEliminated` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GamePlayerEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.gameId)
        statement.bindLong(3, entity.playerId)
        statement.bindText(4, entity.playerName)
        statement.bindLong(5, entity.turnOrder.toLong())
        statement.bindLong(6, entity.currentPhase.toLong())
        statement.bindLong(7, entity.totalScore.toLong())
        val _tmp: Int = if (entity.isEliminated) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindLong(9, entity.id)
      }
    }
  }

  public override suspend fun insertGamePlayers(players: List<GamePlayerEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGamePlayerEntity.insert(_connection, players)
  }

  public override suspend fun updateGamePlayer(player: GamePlayerEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfGamePlayerEntity.handle(_connection, player)
  }

  public override suspend fun updateGamePlayers(players: List<GamePlayerEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfGamePlayerEntity.handleMultiple(_connection, players)
  }

  public override fun getGamePlayers(gameId: Long): Flow<List<GamePlayerEntity>> {
    val _sql: String = "SELECT * FROM game_players WHERE gameId = ? ORDER BY turnOrder ASC"
    return createFlow(__db, false, arrayOf("game_players")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, gameId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGameId: Int = getColumnIndexOrThrow(_stmt, "gameId")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerName: Int = getColumnIndexOrThrow(_stmt, "playerName")
        val _columnIndexOfTurnOrder: Int = getColumnIndexOrThrow(_stmt, "turnOrder")
        val _columnIndexOfCurrentPhase: Int = getColumnIndexOrThrow(_stmt, "currentPhase")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfIsEliminated: Int = getColumnIndexOrThrow(_stmt, "isEliminated")
        val _result: MutableList<GamePlayerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GamePlayerEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpGameId: Long
          _tmpGameId = _stmt.getLong(_columnIndexOfGameId)
          val _tmpPlayerId: Long
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId)
          val _tmpPlayerName: String
          _tmpPlayerName = _stmt.getText(_columnIndexOfPlayerName)
          val _tmpTurnOrder: Int
          _tmpTurnOrder = _stmt.getLong(_columnIndexOfTurnOrder).toInt()
          val _tmpCurrentPhase: Int
          _tmpCurrentPhase = _stmt.getLong(_columnIndexOfCurrentPhase).toInt()
          val _tmpTotalScore: Int
          _tmpTotalScore = _stmt.getLong(_columnIndexOfTotalScore).toInt()
          val _tmpIsEliminated: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEliminated).toInt()
          _tmpIsEliminated = _tmp != 0
          _item = GamePlayerEntity(_tmpId,_tmpGameId,_tmpPlayerId,_tmpPlayerName,_tmpTurnOrder,_tmpCurrentPhase,_tmpTotalScore,_tmpIsEliminated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGamePlayersList(gameId: Long): List<GamePlayerEntity> {
    val _sql: String = "SELECT * FROM game_players WHERE gameId = ? ORDER BY turnOrder ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, gameId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGameId: Int = getColumnIndexOrThrow(_stmt, "gameId")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerName: Int = getColumnIndexOrThrow(_stmt, "playerName")
        val _columnIndexOfTurnOrder: Int = getColumnIndexOrThrow(_stmt, "turnOrder")
        val _columnIndexOfCurrentPhase: Int = getColumnIndexOrThrow(_stmt, "currentPhase")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfIsEliminated: Int = getColumnIndexOrThrow(_stmt, "isEliminated")
        val _result: MutableList<GamePlayerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GamePlayerEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpGameId: Long
          _tmpGameId = _stmt.getLong(_columnIndexOfGameId)
          val _tmpPlayerId: Long
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId)
          val _tmpPlayerName: String
          _tmpPlayerName = _stmt.getText(_columnIndexOfPlayerName)
          val _tmpTurnOrder: Int
          _tmpTurnOrder = _stmt.getLong(_columnIndexOfTurnOrder).toInt()
          val _tmpCurrentPhase: Int
          _tmpCurrentPhase = _stmt.getLong(_columnIndexOfCurrentPhase).toInt()
          val _tmpTotalScore: Int
          _tmpTotalScore = _stmt.getLong(_columnIndexOfTotalScore).toInt()
          val _tmpIsEliminated: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEliminated).toInt()
          _tmpIsEliminated = _tmp != 0
          _item = GamePlayerEntity(_tmpId,_tmpGameId,_tmpPlayerId,_tmpPlayerName,_tmpTurnOrder,_tmpCurrentPhase,_tmpTotalScore,_tmpIsEliminated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGamePlayerById(id: Long): GamePlayerEntity? {
    val _sql: String = "SELECT * FROM game_players WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGameId: Int = getColumnIndexOrThrow(_stmt, "gameId")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerName: Int = getColumnIndexOrThrow(_stmt, "playerName")
        val _columnIndexOfTurnOrder: Int = getColumnIndexOrThrow(_stmt, "turnOrder")
        val _columnIndexOfCurrentPhase: Int = getColumnIndexOrThrow(_stmt, "currentPhase")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfIsEliminated: Int = getColumnIndexOrThrow(_stmt, "isEliminated")
        val _result: GamePlayerEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpGameId: Long
          _tmpGameId = _stmt.getLong(_columnIndexOfGameId)
          val _tmpPlayerId: Long
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId)
          val _tmpPlayerName: String
          _tmpPlayerName = _stmt.getText(_columnIndexOfPlayerName)
          val _tmpTurnOrder: Int
          _tmpTurnOrder = _stmt.getLong(_columnIndexOfTurnOrder).toInt()
          val _tmpCurrentPhase: Int
          _tmpCurrentPhase = _stmt.getLong(_columnIndexOfCurrentPhase).toInt()
          val _tmpTotalScore: Int
          _tmpTotalScore = _stmt.getLong(_columnIndexOfTotalScore).toInt()
          val _tmpIsEliminated: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEliminated).toInt()
          _tmpIsEliminated = _tmp != 0
          _result = GamePlayerEntity(_tmpId,_tmpGameId,_tmpPlayerId,_tmpPlayerName,_tmpTurnOrder,_tmpCurrentPhase,_tmpTotalScore,_tmpIsEliminated)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
