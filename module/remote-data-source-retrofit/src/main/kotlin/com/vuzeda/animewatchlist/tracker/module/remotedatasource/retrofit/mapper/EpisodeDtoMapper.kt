package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto

fun EpisodeDto.toEpisodeInfo(): EpisodeInfo = EpisodeInfo(
    number = malId,
    title = titleRomaji?.takeIf { it.isNotBlank() } ?: title, // Romaji as default, fallback to English
    titleEnglish = title,
    titleJapanese = titleJapanese,
    aired = aired,
    isFiller = filler,
    isRecap = recap
)

fun AnimeEpisodesResponseDto.toEpisodePage(currentPage: Int): EpisodePage = EpisodePage(
    episodes = data.map { it.toEpisodeInfo() },
    hasNextPage = pagination.hasNextPage,
    nextPage = currentPage + 1
)
