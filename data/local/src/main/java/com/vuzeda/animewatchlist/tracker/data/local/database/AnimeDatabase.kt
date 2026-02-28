package com.vuzeda.animewatchlist.tracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity

@Database(
    entities = [AnimeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}
