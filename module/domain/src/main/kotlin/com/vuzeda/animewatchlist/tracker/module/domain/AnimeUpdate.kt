package com.vuzeda.animewatchlist.tracker.module.domain

sealed interface AnimeUpdate {

    data class NewEpisodes(
        val anime: Anime,
        val season: Season,
        val latestAiredEpisode: Int
    ) : AnimeUpdate

    data class NewSeason(
        val anime: Anime,
        val sequelMalId: Int,
        val sequelTitle: String
    ) : AnimeUpdate
}
