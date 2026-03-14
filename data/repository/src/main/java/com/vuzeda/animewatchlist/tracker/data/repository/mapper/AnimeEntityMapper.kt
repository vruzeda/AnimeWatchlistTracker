package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.local.Anime as LocalAnime
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.NotificationType
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

fun LocalAnime.toDomainModel(): Anime = Anime(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = if (genres.isBlank()) emptyList() else genres.split(",").map { it.trim() },
    status = WatchStatus.entries.firstOrNull { it.name == status } ?: WatchStatus.PLAN_TO_WATCH,
    userRating = userRating,
    notificationType = NotificationType.entries.firstOrNull { it.name == notificationType } ?: NotificationType.NONE,
    addedAt = addedAt
)

fun Anime.toLocalModel(): LocalAnime = LocalAnime(
    id = id,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    synopsis = synopsis,
    genres = genres.joinToString(","),
    status = status.name,
    userRating = userRating,
    notificationType = notificationType.name,
    addedAt = addedAt
)
