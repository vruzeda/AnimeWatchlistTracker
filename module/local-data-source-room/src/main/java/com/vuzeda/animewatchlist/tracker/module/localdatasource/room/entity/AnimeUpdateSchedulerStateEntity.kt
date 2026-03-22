package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduler_state")
data class AnimeUpdateSchedulerStateEntity(
    @PrimaryKey val id: Int = 1,
    val lastAnimeUpdateRunAt: Long?
)
