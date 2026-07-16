package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.mapper

import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodePage
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeListPageDto
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto.MalEpisodeRowDto

fun MalEpisodeRowDto.toEpisodeInfo(): EpisodeInfo = EpisodeInfo(
    number = number,
    titleRomaji = titleRomaji?.takeIf { it.isNotBlank() },
    titleEnglish = titleEnglish?.takeIf { it.isNotBlank() },
    titleJapanese = titleJapanese?.takeIf { it.isNotBlank() },
    aired = airedIsoDate,
    isFiller = false,
    isRecap = false
)

fun MalEpisodeListPageDto.toEpisodePage(currentPage: Int): EpisodePage = EpisodePage(
    episodes = episodes.map { it.toEpisodeInfo() },
    hasNextPage = hasNextPage,
    nextPage = currentPage + 1
)