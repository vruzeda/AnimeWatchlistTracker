package com.vuzeda.animewatchlist.tracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vuzeda.animewatchlist.tracker.data.local.dao.AnimeDao
import com.vuzeda.animewatchlist.tracker.data.local.dao.SeasonDao
import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.data.local.entity.SeasonEntity

@Database(
    entities = [AnimeEntity::class, SeasonEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun seasonDao(): SeasonDao
}
