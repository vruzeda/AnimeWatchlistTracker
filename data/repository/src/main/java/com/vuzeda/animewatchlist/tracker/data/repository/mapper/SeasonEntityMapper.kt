package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.local.Season as LocalSeason
import com.vuzeda.animewatchlist.tracker.domain.model.Season

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
