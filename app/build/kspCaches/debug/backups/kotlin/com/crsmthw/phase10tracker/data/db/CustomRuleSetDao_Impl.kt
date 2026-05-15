package com.crsmthw.phase10tracker.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.crsmthw.phase10tracker.`data`.model.CustomRuleSetEntity
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CustomRuleSetDao_Impl(
  __db: RoomDatabase,
) : CustomRuleSetDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCustomRuleSetEntity: EntityInsertAdapter<CustomRuleSetEntity>

  private val __deleteAdapterOfCustomRuleSetEntity: EntityDeleteOrUpdateAdapter<CustomRuleSetEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCustomRuleSetEntity = object : EntityInsertAdapter<CustomRuleSetEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `custom_rule_sets` (`id`,`name`,`rulesJson`,`createdAt`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CustomRuleSetEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.rulesJson)
        statement.bindLong(4, entity.createdAt)
      }
    }
    this.__deleteAdapterOfCustomRuleSetEntity = object : EntityDeleteOrUpdateAdapter<CustomRuleSetEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `custom_rule_sets` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CustomRuleSetEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertRuleSet(ruleSet: CustomRuleSetEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCustomRuleSetEntity.insertAndReturnId(_connection, ruleSet)
    _result
  }

  public override suspend fun deleteRuleSet(ruleSet: CustomRuleSetEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfCustomRuleSetEntity.handle(_connection, ruleSet)
  }

  public override fun getAllRuleSets(): Flow<List<CustomRuleSetEntity>> {
    val _sql: String = "SELECT * FROM custom_rule_sets ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("custom_rule_sets")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfRulesJson: Int = getColumnIndexOrThrow(_stmt, "rulesJson")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CustomRuleSetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CustomRuleSetEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpRulesJson: String
          _tmpRulesJson = _stmt.getText(_columnIndexOfRulesJson)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = CustomRuleSetEntity(_tmpId,_tmpName,_tmpRulesJson,_tmpCreatedAt)
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
