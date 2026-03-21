package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN streamingLinks TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN broadcastInfo TEXT")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN lastEpisodeCheckDate TEXT")
        db.execSQL("UPDATE season SET lastEpisodeCheckDate = date('now')")
        db.execSQL("ALTER TABLE anime ADD COLUMN lastSeasonCheckDate TEXT")
        db.execSQL("UPDATE anime SET lastSeasonCheckDate = date('now')")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN isInWatchlist INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN status TEXT NOT NULL DEFAULT 'PLAN_TO_WATCH'")

        db.execSQL("UPDATE season SET status = (SELECT a.status FROM anime a WHERE a.id = season.animeId)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS anime_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                titleEnglish TEXT,
                titleJapanese TEXT,
                imageUrl TEXT,
                synopsis TEXT,
                genres TEXT NOT NULL DEFAULT '',
                userRating INTEGER,
                notificationType TEXT NOT NULL DEFAULT 'NONE',
                addedAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO anime_new (id, title, titleEnglish, titleJapanese, imageUrl, synopsis, genres, userRating, notificationType, addedAt)
            SELECT id, title, titleEnglish, titleJapanese, imageUrl, synopsis, genres, userRating, notificationType, addedAt
            FROM anime
            """.trimIndent()
        )

        db.execSQL("DROP TABLE anime")
        db.execSQL("ALTER TABLE anime_new RENAME TO anime")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS anime_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                titleEnglish TEXT,
                titleJapanese TEXT,
                imageUrl TEXT,
                synopsis TEXT,
                genres TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL,
                userRating INTEGER,
                notificationType TEXT NOT NULL DEFAULT 'NONE',
                addedAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO anime_new (id, title, titleEnglish, titleJapanese, imageUrl, synopsis, genres, status, userRating, notificationType, addedAt)
            SELECT id, title, titleEnglish, titleJapanese, imageUrl, synopsis, genres, status, userRating,
                CASE WHEN isNotificationsEnabled = 1 THEN 'BOTH' ELSE 'NONE' END,
                addedAt
            FROM anime
            """.trimIndent()
        )

        db.execSQL("DROP TABLE anime")
        db.execSQL("ALTER TABLE anime_new RENAME TO anime")

        db.execSQL(
            "ALTER TABLE season ADD COLUMN isEpisodeNotificationsEnabled INTEGER NOT NULL DEFAULT 0"
        )
    }
}
