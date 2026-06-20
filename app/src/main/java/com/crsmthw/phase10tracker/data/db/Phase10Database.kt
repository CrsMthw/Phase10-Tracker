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

// ── Migration 3 → 4 ───────────────────────────────────────────────────────────
// • Adds resultsSeen flag to games. Default 1 (true) so any already-completed game
//   from a previous version is treated as already acknowledged and never pops a stale
//   winner screen. Newly finished games set it to 0 (see GameDao.finishGame).

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE games ADD COLUMN resultsSeen INTEGER NOT NULL DEFAULT 1")
    }
}

// ── Migration 4 → 5 ───────────────────────────────────────────────────────────
// • Rebuilds game_players WITHOUT the players foreign key (keeps the games FK cascade), so deleting a
//   player no longer wipes their game history. The CREATE TABLE matches Room's generated schema for
//   the updated GamePlayerEntity exactly so the open-time validation passes.

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `game_players_new` (
                `id` INTEGER NOT NULL,
                `gameId` INTEGER NOT NULL,
                `playerId` INTEGER NOT NULL,
                `playerName` TEXT NOT NULL,
                `turnOrder` INTEGER NOT NULL,
                `currentPhase` INTEGER NOT NULL,
                `totalScore` INTEGER NOT NULL,
                `isEliminated` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `game_players_new` (id, gameId, playerId, playerName, turnOrder, currentPhase, totalScore, isEliminated)
            SELECT id, gameId, playerId, playerName, turnOrder, currentPhase, totalScore, isEliminated FROM `game_players`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `game_players`")
        db.execSQL("ALTER TABLE `game_players_new` RENAME TO `game_players`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_players_gameId` ON `game_players` (`gameId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_players_playerId` ON `game_players` (`playerId`)")
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
    version = 5,
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
