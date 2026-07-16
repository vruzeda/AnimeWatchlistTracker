package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite < 3.25 (API 29+) doesn't support ALTER TABLE RENAME COLUMN
        // Recreate episode_info table with title renamed to titleRomaji and new columns
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `episode_info_new` (
                `malId` INTEGER NOT NULL,
                `number` INTEGER NOT NULL,
                `titleRomaji` TEXT,
                `titleEnglish` TEXT,
                `titleJapanese` TEXT,
                `aired` TEXT,
                `isFiller` INTEGER NOT NULL,
                `isRecap` INTEGER NOT NULL,
                PRIMARY KEY(`malId`, `number`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO episode_info_new
                (malId, number, titleRomaji, titleEnglish, titleJapanese, aired, isFiller, isRecap)
            SELECT
                malId, number, title, NULL, NULL, aired, isFiller, isRecap
            FROM episode_info
            """.trimIndent()
        )
        db.execSQL("DROP TABLE episode_info")
        db.execSQL("ALTER TABLE episode_info_new RENAME TO episode_info")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `season_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `animeId` INTEGER NOT NULL,
                `malId` INTEGER NOT NULL UNIQUE,
                `title` TEXT NOT NULL,
                `titleEnglish` TEXT,
                `titleJapanese` TEXT,
                `imageUrl` TEXT,
                `type` TEXT NOT NULL DEFAULT 'TV',
                `episodeCount` INTEGER,
                `status` TEXT NOT NULL DEFAULT 'PLAN_TO_WATCH',
                `score` REAL,
                `orderIndex` INTEGER NOT NULL DEFAULT 0,
                `airingStatus` TEXT,
                `broadcastInfo` TEXT,
                `broadcastDay` TEXT,
                `broadcastTime` TEXT,
                `broadcastTimezone` TEXT,
                `streamingLinks` TEXT NOT NULL DEFAULT '[]',
                `lastCheckedAiredEpisodeCount` INTEGER,
                `latestKnownEpisodeAirDate` TEXT,
                `lastEpisodeCheckPerformedDate` TEXT,
                `isEpisodeNotificationsEnabled` INTEGER NOT NULL DEFAULT 0,
                `isInWatchlist` INTEGER NOT NULL DEFAULT 1,
                `airingSeasonName` TEXT,
                `airingSeasonYear` INTEGER,
                `addedAt` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`animeId`) REFERENCES `anime`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO season_new
                (id, animeId, malId, title, titleEnglish, titleJapanese, imageUrl, type, episodeCount,
                 status, score, orderIndex, airingStatus, broadcastInfo, broadcastDay,
                 broadcastTime, broadcastTimezone, streamingLinks, lastCheckedAiredEpisodeCount,
                 latestKnownEpisodeAirDate, lastEpisodeCheckPerformedDate,
                 isEpisodeNotificationsEnabled, isInWatchlist, airingSeasonName, airingSeasonYear, addedAt)
            SELECT id, animeId, malId, title, titleEnglish, titleJapanese, imageUrl, type, episodeCount,
                   status, score, orderIndex, airingStatus, broadcastInfo, broadcastDay,
                   broadcastTime, broadcastTimezone, streamingLinks, lastCheckedAiredEpisodeCount,
                   latestKnownEpisodeAirDate, lastEpisodeCheckPerformedDate,
                   isEpisodeNotificationsEnabled, isInWatchlist, airingSeasonName, airingSeasonYear, addedAt
            FROM season
            """.trimIndent()
        )

        db.execSQL("DROP TABLE season")
        db.execSQL("ALTER TABLE season_new RENAME TO season")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_season_animeId` ON `season` (`animeId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_season_malId` ON `season` (`malId`)")
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM season WHERE id NOT IN (
                SELECT MIN(id) FROM season
                GROUP BY malId
            )
            """.trimIndent()
        )

        db.execSQL("DROP INDEX IF EXISTS `index_season_animeId_malId`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_season_malId` ON season (malId)")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM season WHERE id NOT IN (
                SELECT MIN(id) FROM season
                GROUP BY animeId, malId
            )
            """.trimIndent()
        )

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_season_animeId_malId` ON season (animeId, malId)")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scheduler_state ADD COLUMN lastAnimeUpdateAttemptRetryCount INTEGER")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scheduler_state ADD COLUMN lastAnimeUpdateAttemptAt INTEGER")
        db.execSQL("ALTER TABLE scheduler_state ADD COLUMN lastAnimeUpdateAttemptResult TEXT")
        db.execSQL("ALTER TABLE scheduler_state ADD COLUMN lastAnimeUpdateAttemptFailureReason TEXT")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `anime_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `titleEnglish` TEXT,
                `titleJapanese` TEXT,
                `imageUrl` TEXT,
                `synopsis` TEXT,
                `genres` TEXT NOT NULL DEFAULT '',
                `userRating` INTEGER,
                `notificationType` TEXT NOT NULL DEFAULT 'NONE',
                `latestKnownSeasonStartDate` TEXT,
                `lastSeasonCheckPerformedDate` TEXT,
                `addedAt` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO anime_new
                (id, title, titleEnglish, titleJapanese, imageUrl, synopsis, genres, userRating,
                 notificationType, latestKnownSeasonStartDate, lastSeasonCheckPerformedDate, addedAt)
            SELECT id, title, titleEnglish, titleJapanese, imageUrl, synopsis, genres, userRating,
                   notificationType, lastSeasonCheckDate, NULL, addedAt
            FROM anime
            """.trimIndent()
        )
        db.execSQL("DROP TABLE anime")
        db.execSQL("ALTER TABLE anime_new RENAME TO anime")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `season_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `animeId` INTEGER NOT NULL,
                `malId` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `titleEnglish` TEXT,
                `titleJapanese` TEXT,
                `imageUrl` TEXT,
                `type` TEXT NOT NULL DEFAULT 'TV',
                `episodeCount` INTEGER,
                `currentEpisode` INTEGER NOT NULL DEFAULT 0,
                `status` TEXT NOT NULL DEFAULT 'PLAN_TO_WATCH',
                `score` REAL,
                `orderIndex` INTEGER NOT NULL DEFAULT 0,
                `airingStatus` TEXT,
                `broadcastInfo` TEXT,
                `broadcastDay` TEXT,
                `broadcastTime` TEXT,
                `broadcastTimezone` TEXT,
                `streamingLinks` TEXT NOT NULL DEFAULT '',
                `lastCheckedAiredEpisodeCount` INTEGER,
                `latestKnownEpisodeAirDate` TEXT,
                `lastEpisodeCheckPerformedDate` TEXT,
                `isEpisodeNotificationsEnabled` INTEGER NOT NULL DEFAULT 0,
                `isInWatchlist` INTEGER NOT NULL DEFAULT 1,
                `airingSeasonName` TEXT,
                `airingSeasonYear` INTEGER,
                `addedAt` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`animeId`) REFERENCES `anime`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO season_new
                (id, animeId, malId, title, titleEnglish, titleJapanese, imageUrl, type, episodeCount,
                 currentEpisode, status, score, orderIndex, airingStatus, broadcastInfo, broadcastDay,
                 broadcastTime, broadcastTimezone, streamingLinks, lastCheckedAiredEpisodeCount,
                 latestKnownEpisodeAirDate, lastEpisodeCheckPerformedDate,
                 isEpisodeNotificationsEnabled, isInWatchlist, airingSeasonName, airingSeasonYear, addedAt)
            SELECT id, animeId, malId, title, titleEnglish, titleJapanese, imageUrl, type, episodeCount,
                   currentEpisode, status, score, orderIndex, airingStatus, broadcastInfo, broadcastDay,
                   broadcastTime, broadcastTimezone, streamingLinks, lastCheckedAiredEpisodeCount,
                   lastEpisodeCheckDate, NULL,
                   isEpisodeNotificationsEnabled, isInWatchlist, airingSeasonName, airingSeasonYear, addedAt
            FROM season
            """.trimIndent()
        )
        db.execSQL("DROP TABLE season")
        db.execSQL("ALTER TABLE season_new RENAME TO season")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_season_animeId` ON `season` (`animeId`)")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `episode_info` " +
                "(`malId` INTEGER NOT NULL, `number` INTEGER NOT NULL, " +
                "`title` TEXT, `aired` TEXT, `isFiller` INTEGER NOT NULL, `isRecap` INTEGER NOT NULL, " +
                "PRIMARY KEY(`malId`, `number`))"
        )
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE season SET addedAt = (SELECT addedAt FROM anime WHERE id = season.animeId)")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN airingSeasonName TEXT")
        db.execSQL("ALTER TABLE season ADD COLUMN airingSeasonYear INTEGER")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `watched_episode` " +
                "(`seasonId` INTEGER NOT NULL, `episodeNumber` INTEGER NOT NULL, " +
                "PRIMARY KEY(`seasonId`, `episodeNumber`), " +
                "FOREIGN KEY(`seasonId`) REFERENCES `season`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_watched_episode_seasonId` ON `watched_episode` (`seasonId`)"
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `scheduler_state` (`id` INTEGER NOT NULL, `lastAnimeUpdateRunAt` INTEGER, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE season ADD COLUMN broadcastDay TEXT")
        db.execSQL("ALTER TABLE season ADD COLUMN broadcastTime TEXT")
        db.execSQL("ALTER TABLE season ADD COLUMN broadcastTimezone TEXT")
    }
}

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
