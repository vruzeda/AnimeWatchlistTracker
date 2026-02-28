package com.vuzeda.animewatchlist.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val malId: Int? = null,
    val title: String,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val episodeCount: Int? = null,
    val currentEpisode: Int = 0,
    val score: Double? = null,
    val userRating: Int? = null,
    val status: String,
    val genres: String,
    val isNotificationsEnabled: Int = 0,
    val lastCheckedEpisodeCount: Int? = null,
    val knownSequelMalIds: String = ""
)
