package com.crsmthw.phase10tracker.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.crsmthw.phase10tracker.`data`.model.PlayerEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlayerDao_Impl(
  __db: RoomDatabase,
) : PlayerDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlayerEntity: EntityInsertAdapter<PlayerEntity>

  private val __deleteAdapterOfPlayerEntity: EntityDeleteOrUpdateAdapter<PlayerEntity>

  private val __updateAdapterOfPlayerEntity: EntityDeleteOrUpdateAdapter<PlayerEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPlayerEntity = object : EntityInsertAdapter<PlayerEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `players` (`id`,`name`,`gamesPlayed`,`gamesWon`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlayerEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.gamesPlayed.toLong())
        statement.bindLong(4, entity.gamesWon.toLong())
        statement.bindLong(5, entity.createdAt)
      }
    }
    this.__deleteAdapterOfPlayerEntity = object : EntityDeleteOrUpdateAdapter<PlayerEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `players` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlayerEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfPlayerEntity = object : EntityDeleteOrUpdateAdapter<PlayerEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `players` SET `id` = ?,`name` = ?,`gamesPlayed` = ?,`gamesWon` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlayerEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.gamesPlayed.toLong())
        statement.bindLong(4, entity.gamesWon.toLong())
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insertPlayer(player: PlayerEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPlayerEntity.insertAndReturnId(_connection, player)
    _result
  }

  public override suspend fun deletePlayer(player: PlayerEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPlayerEntity.handle(_connection, player)
  }

  public override suspend fun updatePlayer(player: PlayerEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlayerEntity.handle(_connection, player)
  }

  public override fun getAllPlayers(): Flow<List<PlayerEntity>> {
    val _sql: String = "SELECT * FROM players ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("players")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGamesPlayed: Int = getColumnIndexOrThrow(_stmt, "gamesPlayed")
        val _columnIndexOfGamesWon: Int = getColumnIndexOrThrow(_stmt, "gamesWon")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<PlayerEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlayerEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGamesPlayed: Int
          _tmpGamesPlayed = _stmt.getLong(_columnIndexOfGamesPlayed).toInt()
          val _tmpGamesWon: Int
          _tmpGamesWon = _stmt.getLong(_columnIndexOfGamesWon).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = PlayerEntity(_tmpId,_tmpName,_tmpGamesPlayed,_tmpGamesWon,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlayerById(id: Long): PlayerEntity? {
    val _sql: String = "SELECT * FROM players WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfGamesPlayed: Int = getColumnIndexOrThrow(_stmt, "gamesPlayed")
        val _columnIndexOfGamesWon: Int = getColumnIndexOrThrow(_stmt, "gamesWon")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: PlayerEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpGamesPlayed: Int
          _tmpGamesPlayed = _stmt.getLong(_columnIndexOfGamesPlayed).toInt()
          val _tmpGamesWon: Int
          _tmpGamesWon = _stmt.getLong(_columnIndexOfGamesWon).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = PlayerEntity(_tmpId,_tmpName,_tmpGamesPlayed,_tmpGamesWon,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementGamesPlayed(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE players SET gamesPlayed = gamesPlayed + 1 WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementGamesWon(id: Long) {
    val _sql: String = "UPDATE players SET gamesWon = gamesWon + 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
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
