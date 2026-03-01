package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.local.entity.AnimeEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.KnownSequel
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
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    knownSequels = parseKnownSequelData(knownSequelData),
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
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    knownSequelData = serializeKnownSequelData(knownSequels),
    addedAt = addedAt
)

fun parseKnownSequelData(data: String): List<KnownSequel> {
    if (data.isBlank()) return emptyList()
    return data.split(",").mapNotNull { entry ->
        val parts = entry.trim().split(":")
        when (parts.size) {
            2 -> KnownSequel(
                malId = parts[0].toIntOrNull() ?: return@mapNotNull null,
                notified = parts[1].toBooleanStrictOrNull() ?: return@mapNotNull null
            )
            1 -> parts[0].toIntOrNull()?.let { KnownSequel(malId = it, notified = true) }
            else -> null
        }
    }
}

fun serializeKnownSequelData(sequels: List<KnownSequel>): String =
    sequels.joinToString(",") { "${it.malId}:${it.notified}" }
