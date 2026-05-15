package com.crsmthw.phase10tracker.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.crsmthw.phase10tracker.`data`.model.RoundEntity
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
public class RoundDao_Impl(
  __db: RoomDatabase,
) : RoundDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRoundEntity: EntityInsertAdapter<RoundEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfRoundEntity = object : EntityInsertAdapter<RoundEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `rounds` (`id`,`gameId`,`gamePlayerId`,`roundNumber`,`score`,`phaseCompleted`,`phaseAtRoundStart`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RoundEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.gameId)
        statement.bindLong(3, entity.gamePlayerId)
        statement.bindLong(4, entity.roundNumber.toLong())
        statement.bindLong(5, entity.score.toLong())
        val _tmp: Int = if (entity.phaseCompleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.phaseAtRoundStart.toLong())
      }
    }
  }

  public override suspend fun insertRounds(rounds: List<RoundEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRoundEntity.insert(_connection, rounds)
  }

  public override fun getRoundsForGame(gameId: Long): Flow<List<RoundEntity>> {
    val _sql: String = "SELECT * FROM rounds WHERE gameId = ? ORDER BY roundNumber ASC"
    return createFlow(__db, false, arrayOf("rounds")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, gameId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGameId: Int = getColumnIndexOrThrow(_stmt, "gameId")
        val _columnIndexOfGamePlayerId: Int = getColumnIndexOrThrow(_stmt, "gamePlayerId")
        val _columnIndexOfRoundNumber: Int = getColumnIndexOrThrow(_stmt, "roundNumber")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "score")
        val _columnIndexOfPhaseCompleted: Int = getColumnIndexOrThrow(_stmt, "phaseCompleted")
        val _columnIndexOfPhaseAtRoundStart: Int = getColumnIndexOrThrow(_stmt, "phaseAtRoundStart")
        val _result: MutableList<RoundEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RoundEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpGameId: Long
          _tmpGameId = _stmt.getLong(_columnIndexOfGameId)
          val _tmpGamePlayerId: Long
          _tmpGamePlayerId = _stmt.getLong(_columnIndexOfGamePlayerId)
          val _tmpRoundNumber: Int
          _tmpRoundNumber = _stmt.getLong(_columnIndexOfRoundNumber).toInt()
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpPhaseCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPhaseCompleted).toInt()
          _tmpPhaseCompleted = _tmp != 0
          val _tmpPhaseAtRoundStart: Int
          _tmpPhaseAtRoundStart = _stmt.getLong(_columnIndexOfPhaseAtRoundStart).toInt()
          _item = RoundEntity(_tmpId,_tmpGameId,_tmpGamePlayerId,_tmpRoundNumber,_tmpScore,_tmpPhaseCompleted,_tmpPhaseAtRoundStart)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRoundsForPlayer(gamePlayerId: Long): List<RoundEntity> {
    val _sql: String = "SELECT * FROM rounds WHERE gamePlayerId = ? ORDER BY roundNumber ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, gamePlayerId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGameId: Int = getColumnIndexOrThrow(_stmt, "gameId")
        val _columnIndexOfGamePlayerId: Int = getColumnIndexOrThrow(_stmt, "gamePlayerId")
        val _columnIndexOfRoundNumber: Int = getColumnIndexOrThrow(_stmt, "roundNumber")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "score")
        val _columnIndexOfPhaseCompleted: Int = getColumnIndexOrThrow(_stmt, "phaseCompleted")
        val _columnIndexOfPhaseAtRoundStart: Int = getColumnIndexOrThrow(_stmt, "phaseAtRoundStart")
        val _result: MutableList<RoundEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RoundEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpGameId: Long
          _tmpGameId = _stmt.getLong(_columnIndexOfGameId)
          val _tmpGamePlayerId: Long
          _tmpGamePlayerId = _stmt.getLong(_columnIndexOfGamePlayerId)
          val _tmpRoundNumber: Int
          _tmpRoundNumber = _stmt.getLong(_columnIndexOfRoundNumber).toInt()
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpPhaseCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPhaseCompleted).toInt()
          _tmpPhaseCompleted = _tmp != 0
          val _tmpPhaseAtRoundStart: Int
          _tmpPhaseAtRoundStart = _stmt.getLong(_columnIndexOfPhaseAtRoundStart).toInt()
          _item = RoundEntity(_tmpId,_tmpGameId,_tmpGamePlayerId,_tmpRoundNumber,_tmpScore,_tmpPhaseCompleted,_tmpPhaseAtRoundStart)
          _result.add(_item)
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
