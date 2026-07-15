package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto

fun MalEpisodeRowDto.toEpisodeInfo(): EpisodeInfo = EpisodeInfo(
    number = number,
    title = titleJapanese?.takeIf { it.isNotBlank() } ?: titleEnglish ?: title,
    titleEnglish = titleEnglish,
    titleJapanese = titleJapanese,
    aired = airedIsoDate,
    isFiller = false,
    isRecap = false
)

fun MalEpisodeListPageDto.toEpisodePage(currentPage: Int): EpisodePage = EpisodePage(
    episodes = episodes.map { it.toEpisodeInfo() },
    hasNextPage = hasNextPage,
    nextPage = currentPage + 1
)
