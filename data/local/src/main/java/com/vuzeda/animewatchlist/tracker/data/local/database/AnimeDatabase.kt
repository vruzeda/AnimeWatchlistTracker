package com.vuzeda.animewatchlist.tracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity

@Database(
    entities = [AnimeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE anime ADD COLUMN isNotificationsEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE anime ADD COLUMN lastCheckedEpisodeCount INTEGER")
                db.execSQL("ALTER TABLE anime ADD COLUMN knownSequelMalIds TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
