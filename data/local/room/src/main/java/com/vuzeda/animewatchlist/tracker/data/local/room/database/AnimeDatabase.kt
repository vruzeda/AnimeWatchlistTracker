package com.vuzeda.animewatchlist.tracker.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vuzeda.animewatchlist.tracker.data.local.room.dao.AnimeRoomDao
import com.vuzeda.animewatchlist.tracker.data.local.room.dao.SeasonRoomDao
import com.vuzeda.animewatchlist.tracker.data.local.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.data.local.room.entity.SeasonEntity

@Database(
    entities = [AnimeEntity::class, SeasonEntity::class],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeRoomDao
    abstract fun seasonDao(): SeasonRoomDao
}
