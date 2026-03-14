package com.vuzeda.animewatchlist.tracker.module.repository.mapper

import com.vuzeda.animewatchlist.tracker.module.localdatasource.Season as LocalSeason
import com.vuzeda.animewatchlist.tracker.module.domain.Season

fun LocalSeason.toDomainModel(): Season = Season(
    id = id,
    animeId = animeId,
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    type = type,
    episodeCount = episodeCount,
    currentEpisode = currentEpisode,
    score = score,
    orderIndex = orderIndex,
    airingStatus = airingStatus,
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    isEpisodeNotificationsEnabled = isEpisodeNotificationsEnabled
)

fun Season.toLocalModel(): LocalSeason = LocalSeason(
    id = id,
    animeId = animeId,
    malId = malId,
    title = title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    imageUrl = imageUrl,
    type = type,
    episodeCount = episodeCount,
    currentEpisode = currentEpisode,
    score = score,
    orderIndex = orderIndex,
    airingStatus = airingStatus,
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount,
    isEpisodeNotificationsEnabled = isEpisodeNotificationsEnabled
)
