package com.vuzeda.animewatchlist.tracker.module.localdatasource.room

import androidx.room.Room
import com.vuzeda.animewatchlist.tracker.module.localdatasource.room.database.AnimeDatabase
import org.robolectric.RuntimeEnvironment

object RoomDatabaseTestHelper {
    fun createInMemoryDatabase(): AnimeDatabase {
        val context = RuntimeEnvironment.getApplication()
        return Room.inMemoryDatabaseBuilder(context, AnimeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}
