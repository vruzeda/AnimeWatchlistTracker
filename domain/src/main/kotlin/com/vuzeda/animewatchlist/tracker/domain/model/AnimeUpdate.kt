package com.vuzeda.animewatchlist.tracker.domain.model

sealed interface AnimeUpdate {

    data class NewEpisodes(
        val anime: Anime,
        val previousCount: Int,
        val currentCount: Int
    ) : AnimeUpdate

    data class NewSeason(
        val anime: Anime,
        val sequelMalId: Int,
        val sequelTitle: String
    ) : AnimeUpdate
}
