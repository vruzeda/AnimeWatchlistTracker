package com.vuzeda.animewatchlist.tracker.data.repository.mapper

import com.vuzeda.animewatchlist.tracker.data.api.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.data.api.dto.EpisodeDto
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.domain.model.EpisodePage

fun EpisodeDto.toEpisodeInfo(): EpisodeInfo = EpisodeInfo(
    number = malId,
    title = title,
    aired = aired,
    isFiller = filler,
    isRecap = recap
)

fun AnimeEpisodesResponseDto.toEpisodePage(currentPage: Int): EpisodePage = EpisodePage(
    episodes = data.map { it.toEpisodeInfo() },
    hasNextPage = pagination.hasNextPage,
    nextPage = currentPage + 1
)
