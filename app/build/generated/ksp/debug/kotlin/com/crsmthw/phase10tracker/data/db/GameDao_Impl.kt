package com.crsmthw.phase10tracker.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.crsmthw.phase10tracker.`data`.model.GameEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GameDao_Impl(
  __db: RoomDatabase,
) : GameDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGameEntity: EntityInsertAdapter<GameEntity>

  private val __updateAdapterOfGameEntity: EntityDeleteOrUpdateAdapter<GameEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfGameEntity = object : EntityInsertAdapter<GameEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `games` (`id`,`startedAt`,`finishedAt`,`isComplete`,`winnerId`,`currentRound`,`currentDealerIndex`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GameEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.startedAt)
        val _tmpFinishedAt: Long? = entity.finishedAt
        if (_tmpFinishedAt == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpFinishedAt)
        }
        val _tmp: Int = if (entity.isComplete) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmpWinnerId: Long? = entity.winnerId
        if (_tmpWinnerId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpWinnerId)
        }
        statement.bindLong(6, entity.currentRound.toLong())
        statement.bindLong(7, entity.currentDealerIndex.toLong())
      }
    }
    this.__updateAdapterOfGameEntity = object : EntityDeleteOrUpdateAdapter<GameEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `games` SET `id` = ?,`startedAt` = ?,`finishedAt` = ?,`isComplete` = ?,`winnerId` = ?,`currentRound` = ?,`currentDealerIndex` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GameEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.startedAt)
        val _tmpFinishedAt: Long? = entity.finishedAt
        if (_tmpFinishedAt == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpFinishedAt)
        }
        val _tmp: Int = if (entity.isComplete) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmpWinnerId: Long? = entity.winnerId
        if (_tmpWinnerId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpWinnerId)
        }
        statement.bindLong(6, entity.currentRound.toLong())
        statement.bindLong(7, entity.currentDealerIndex.toLong())
        statement.bindLong(8, entity.id)
      }
    }
  }

  public override suspend fun insertGame(game: GameEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfGameEntity.insertAndReturnId(_connection, game)
    _result
  }

  public override suspend fun updateGame(game: GameEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfGameEntity.handle(_connection, game)
  }

  public override suspend fun getActiveGame(): GameEntity? {
    val _sql: String = "SELECT * FROM games WHERE isComplete = 0 ORDER BY startedAt DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfFinishedAt: Int = getColumnIndexOrThrow(_stmt, "finishedAt")
        val _columnIndexOfIsComplete: Int = getColumnIndexOrThrow(_stmt, "isComplete")
        val _columnIndexOfWinnerId: Int = getColumnIndexOrThrow(_stmt, "winnerId")
        val _columnIndexOfCurrentRound: Int = getColumnIndexOrThrow(_stmt, "currentRound")
        val _columnIndexOfCurrentDealerIndex: Int = getColumnIndexOrThrow(_stmt, "currentDealerIndex")
        val _result: GameEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpFinishedAt: Long?
          if (_stmt.isNull(_columnIndexOfFinishedAt)) {
            _tmpFinishedAt = null
          } else {
            _tmpFinishedAt = _stmt.getLong(_columnIndexOfFinishedAt)
          }
          val _tmpIsComplete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsComplete).toInt()
          _tmpIsComplete = _tmp != 0
          val _tmpWinnerId: Long?
          if (_stmt.isNull(_columnIndexOfWinnerId)) {
            _tmpWinnerId = null
          } else {
            _tmpWinnerId = _stmt.getLong(_columnIndexOfWinnerId)
          }
          val _tmpCurrentRound: Int
          _tmpCurrentRound = _stmt.getLong(_columnIndexOfCurrentRound).toInt()
          val _tmpCurrentDealerIndex: Int
          _tmpCurrentDealerIndex = _stmt.getLong(_columnIndexOfCurrentDealerIndex).toInt()
          _result = GameEntity(_tmpId,_tmpStartedAt,_tmpFinishedAt,_tmpIsComplete,_tmpWinnerId,_tmpCurrentRound,_tmpCurrentDealerIndex)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActiveGame(): Flow<GameEntity?> {
    val _sql: String = "SELECT * FROM games WHERE isComplete = 0 ORDER BY startedAt DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("games")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfFinishedAt: Int = getColumnIndexOrThrow(_stmt, "finishedAt")
        val _columnIndexOfIsComplete: Int = getColumnIndexOrThrow(_stmt, "isComplete")
        val _columnIndexOfWinnerId: Int = getColumnIndexOrThrow(_stmt, "winnerId")
        val _columnIndexOfCurrentRound: Int = getColumnIndexOrThrow(_stmt, "currentRound")
        val _columnIndexOfCurrentDealerIndex: Int = getColumnIndexOrThrow(_stmt, "currentDealerIndex")
        val _result: GameEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpFinishedAt: Long?
          if (_stmt.isNull(_columnIndexOfFinishedAt)) {
            _tmpFinishedAt = null
          } else {
            _tmpFinishedAt = _stmt.getLong(_columnIndexOfFinishedAt)
          }
          val _tmpIsComplete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsComplete).toInt()
          _tmpIsComplete = _tmp != 0
          val _tmpWinnerId: Long?
          if (_stmt.isNull(_columnIndexOfWinnerId)) {
            _tmpWinnerId = null
          } else {
            _tmpWinnerId = _stmt.getLong(_columnIndexOfWinnerId)
          }
          val _tmpCurrentRound: Int
          _tmpCurrentRound = _stmt.getLong(_columnIndexOfCurrentRound).toInt()
          val _tmpCurrentDealerIndex: Int
          _tmpCurrentDealerIndex = _stmt.getLong(_columnIndexOfCurrentDealerIndex).toInt()
          _result = GameEntity(_tmpId,_tmpStartedAt,_tmpFinishedAt,_tmpIsComplete,_tmpWinnerId,_tmpCurrentRound,_tmpCurrentDealerIndex)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getGameById(id: Long): Flow<GameEntity?> {
    val _sql: String = "SELECT * FROM games WHERE id = ?"
    return createFlow(__db, false, arrayOf("games")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfFinishedAt: Int = getColumnIndexOrThrow(_stmt, "finishedAt")
        val _columnIndexOfIsComplete: Int = getColumnIndexOrThrow(_stmt, "isComplete")
        val _columnIndexOfWinnerId: Int = getColumnIndexOrThrow(_stmt, "winnerId")
        val _columnIndexOfCurrentRound: Int = getColumnIndexOrThrow(_stmt, "currentRound")
        val _columnIndexOfCurrentDealerIndex: Int = getColumnIndexOrThrow(_stmt, "currentDealerIndex")
        val _result: GameEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpFinishedAt: Long?
          if (_stmt.isNull(_columnIndexOfFinishedAt)) {
            _tmpFinishedAt = null
          } else {
            _tmpFinishedAt = _stmt.getLong(_columnIndexOfFinishedAt)
          }
          val _tmpIsComplete: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsComplete).toInt()
          _tmpIsComplete = _tmp != 0
          val _tmpWinnerId: Long?
          if (_stmt.isNull(_columnIndexOfWinnerId)) {
            _tmpWinnerId = null
          } else {
            _tmpWinnerId = _stmt.getLong(_columnIndexOfWinnerId)
          }
          val _tmpCurrentRound: Int
          _tmpCurrentRound = _stmt.getLong(_columnIndexOfCurrentRound).toInt()
          val _tmpCurrentDealerIndex: Int
          _tmpCurrentDealerIndex = _stmt.getLong(_columnIndexOfCurrentDealerIndex).toInt()
          _result = GameEntity(_tmpId,_tmpStartedAt,_tmpFinishedAt,_tmpIsComplete,_tmpWinnerId,_tmpCurrentRound,_tmpCurrentDealerIndex)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun cancelAllIncompleteGames(now: Long) {
    val _sql: String = "UPDATE games SET isComplete = 1, finishedAt = ? WHERE isComplete = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, now)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun advanceRound(
    id: Long,
    round: Int,
    dealerIndex: Int,
  ) {
    val _sql: String = "UPDATE games SET currentRound = ?, currentDealerIndex = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, round.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, dealerIndex.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun finishGame(
    id: Long,
    winnerId: Long,
    finishedAt: Long,
  ) {
    val _sql: String = """
        |
        |        UPDATE games 
        |        SET isComplete = 1, finishedAt = ?, winnerId = ? 
        |        WHERE id = ?
        |    
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, finishedAt)
        _argIndex = 2
        _stmt.bindLong(_argIndex, winnerId)
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
