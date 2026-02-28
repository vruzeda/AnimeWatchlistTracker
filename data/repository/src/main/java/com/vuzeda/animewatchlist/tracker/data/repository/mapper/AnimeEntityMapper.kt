package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

fun AnimeEntity.toDomainModel(): Anime = Anime(
    id = id,
    malId = malId,
    title = title,
    imageUrl = imageUrl,
    synopsis = synopsis,
    episodeCount = episodeCount,
    currentEpisode = currentEpisode,
    score = score,
    userRating = userRating,
    status = WatchStatus.entries.firstOrNull { it.name == status } ?: WatchStatus.PLAN_TO_WATCH,
    genres = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() },
    isNotificationsEnabled = isNotificationsEnabled == 1,
    lastCheckedEpisodeCount = lastCheckedEpisodeCount,
    knownSequelMalIds = if (knownSequelMalIds.isBlank()) emptyList()
        else knownSequelMalIds.split(",").map { it.trim().toInt() },
    addedAt = addedAt
)

fun Anime.toEntity(): AnimeEntity = AnimeEntity(
    id = id,
    malId = malId,
    title = title,
    imageUrl = imageUrl,
    synopsis = synopsis,
    episodeCount = episodeCount,
    currentEpisode = currentEpisode,
    score = score,
    userRating = userRating,
    status = status.name,
    genres = genres.joinToString(","),
    isNotificationsEnabled = if (isNotificationsEnabled) 1 else 0,
    lastCheckedEpisodeCount = lastCheckedEpisodeCount,
    knownSequelMalIds = knownSequelMalIds.joinToString(","),
    addedAt = addedAt
)
