package com.vuzeda.animewatchlist.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: String = "",
    val status: String,
    val userRating: Int? = null,
    val isNotificationsEnabled: Boolean = false,
    val addedAt: Long = 0
)
