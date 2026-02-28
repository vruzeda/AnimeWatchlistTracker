package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeDataDto
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus

fun AnimeDataDto.toDomainModel(): Anime = Anime(
    id = 0,
    malId = malId,
    title = title,
    imageUrl = images?.jpg?.largeImageUrl ?: images?.jpg?.imageUrl,
    synopsis = synopsis,
    episodeCount = episodes,
    currentEpisode = 0,
    score = score,
    userRating = null,
    status = WatchStatus.PLAN_TO_WATCH,
    genres = genres?.map { it.name } ?: emptyList()
)
