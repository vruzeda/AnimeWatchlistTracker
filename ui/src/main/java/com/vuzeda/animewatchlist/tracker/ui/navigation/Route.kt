package com.vuzeda.animewatchlist.tracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object Seasons : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class AnimeDetail(val animeId: Long = 0, val malId: Int = 0) : Route

    @Serializable
    data class SeasonDetail(val seasonId: Long = 0, val malId: Int = 0) : Route
}
