package com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit.dto

data class MalEpisodeListPageDto(
    val episodes: List<MalEpisodeRowDto>,
    val hasNextPage: Boolean
)

data class MalEpisodeRowDto(
    val number: Int,
    val title: String?,
    val airedIsoDate: String?
)
