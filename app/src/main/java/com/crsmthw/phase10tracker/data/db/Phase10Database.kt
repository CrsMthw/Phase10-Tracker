package com.crsmthw.phase10tracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crsmthw.phase10tracker.data.model.*

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        CustomRuleSetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class Phase10Database : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun roundDao(): RoundDao
    abstract fun customRuleSetDao(): CustomRuleSetDao

    companion object {
        @Volatile
        private var INSTANCE: Phase10Database? = null

        fun getInstance(context: Context): Phase10Database {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    Phase10Database::class.java,
                    "phase10_tracker.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
