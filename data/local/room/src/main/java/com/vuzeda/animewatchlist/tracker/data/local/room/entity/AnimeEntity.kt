package com.vuzeda.animewatchlist.tracker.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vuzeda.animewatchlist.tracker.data.local.Anime as LocalAnime

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
    val notificationType: String = "NONE",
    val addedAt: Long = 0
)

fun AnimeEntity.toLocalModel(): LocalAnime = LocalAnime(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = genres,
    status = status,
    userRating = userRating,
    notificationType = notificationType,
    addedAt = addedAt
)

fun LocalAnime.toEntity(): AnimeEntity = AnimeEntity(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = genres,
    status = status,
    userRating = userRating,
    notificationType = notificationType,
    addedAt = addedAt
)
