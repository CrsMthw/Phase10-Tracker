package com.crsmthw.phase10tracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crsmthw.phase10tracker.data.model.*

// ── Migration 2 → 3 ───────────────────────────────────────────────────────────
// • Adds phaseSetId column to games table
// • Renames custom_rule_sets table to custom_phase_sets

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Store which phase set was used when the game was created
        db.execSQL("ALTER TABLE games ADD COLUMN phaseSetId INTEGER NOT NULL DEFAULT -1")
        // Rename table to match the updated entity name
        db.execSQL("ALTER TABLE custom_rule_sets RENAME TO custom_phase_sets")
    }
}

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        CustomPhaseSetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class Phase10Database : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun roundDao(): RoundDao
    abstract fun customPhaseSetDao(): CustomPhaseSetDao

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
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
