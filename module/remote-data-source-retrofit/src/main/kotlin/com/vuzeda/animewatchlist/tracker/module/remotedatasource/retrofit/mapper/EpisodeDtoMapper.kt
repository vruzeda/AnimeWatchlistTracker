package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.AnimeEpisodesResponseDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.EpisodeDto

fun EpisodeDto.toEpisodeInfo(): EpisodeInfo = EpisodeInfo(
    number = malId,
    titleRomaji = titleRomaji?.takeIf { it.isNotBlank() },
    titleEnglish = title?.takeIf { it.isNotBlank() },
    titleJapanese = titleJapanese?.takeIf { it.isNotBlank() },
    aired = aired,
    isFiller = filler,
    isRecap = recap
)

fun AnimeEpisodesResponseDto.toEpisodePage(currentPage: Int): EpisodePage = EpisodePage(
    episodes = data.map { it.toEpisodeInfo() },
    hasNextPage = pagination.hasNextPage,
    nextPage = currentPage + 1
)