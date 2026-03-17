package com.vuzeda.animewatchlist.tracker.module.localdatasource.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import java.time.LocalDate

@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val titleEnglish: String? = null,
    val titleJapanese: String? = null,
    val imageUrl: String? = null,
    val synopsis: String? = null,
    val genres: String = "",
    val userRating: Int? = null,
    val notificationType: String = "NONE",
    val lastSeasonCheckDate: LocalDate? = null,
    val addedAt: Long = 0
)

fun AnimeEntity.toDomainModel(): Anime = Anime(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() },
    userRating = userRating,
    notificationType = NotificationType.entries.firstOrNull { it.name == notificationType } ?: NotificationType.NONE,
    lastSeasonCheckDate = lastSeasonCheckDate,
    addedAt = addedAt
)

fun Anime.toEntity(): AnimeEntity = AnimeEntity(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = genres.joinToString(","),
    userRating = userRating,
    notificationType = notificationType.name,
    lastSeasonCheckDate = lastSeasonCheckDate,
    addedAt = addedAt
)
