package com.vuzeda.animewatchlist.tracker.module.localdatasource.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.Converters
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.AnimeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.SeasonRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.WatchedEpisodeRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.dao.EpisodeInfoRoomDao
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.WatchedEpisodeEntity
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity.EpisodeInfoEntity
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

// Test extension to provide an in-memory database for DAO tests
class RoomDatabaseTestExtension : BeforeEachCallback {
    private lateinit var database: AnimeDatabase

    override fun beforeEach(context: ExtensionContext?) {
        // This would need Android context in real usage; for now provide the infrastructure
    }

    fun getDatabase(): AnimeDatabase = database
}
