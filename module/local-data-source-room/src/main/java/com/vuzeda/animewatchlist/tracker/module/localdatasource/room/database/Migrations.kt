package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
