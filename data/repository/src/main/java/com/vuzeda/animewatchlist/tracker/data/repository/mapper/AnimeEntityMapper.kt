package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

fun AnimeEntity.toDomainModel(): Anime = Anime(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() },
    status = WatchStatus.entries.firstOrNull { it.name == status } ?: WatchStatus.PLAN_TO_WATCH,
    userRating = userRating,
    isNotificationsEnabled = isNotificationsEnabled,
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
    status = status.name,
    userRating = userRating,
    isNotificationsEnabled = isNotificationsEnabled,
    addedAt = addedAt
)
