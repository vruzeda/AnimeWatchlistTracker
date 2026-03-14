package com.vuzeda.animewatchlist.tracker.module.repository.mapper

import com.vuzeda.animewatchlist.tracker.module.localdatasource.Anime as LocalAnime
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus

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
