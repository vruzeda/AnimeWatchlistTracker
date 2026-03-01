package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.local.entity.SeasonEntity
import com.vuzeda.animewatchlist.tracker.domain.model.Season

fun SeasonEntity.toDomainModel(): Season = Season(
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
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount
)

fun Season.toEntity(): SeasonEntity = SeasonEntity(
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
    lastCheckedAiredEpisodeCount = lastCheckedAiredEpisodeCount
)
