package com.vuzeda.animewatchlist.tracker.ui.navigation

sealed interface Route {
    val route: String

    data object Home : Route {
        override val route = "home"
    }

    data object Search : Route {
        override val route = "search"
    }

    data class AnimeDetail(val animeId: Long = 0, val malId: Int = 0) : Route {
        override val route: String
            get() = if (malId > 0) "anime_detail/$animeId?malId=$malId" else "anime_detail/$animeId"

        companion object {
            const val ROUTE_PATTERN = "anime_detail/{animeId}?malId={malId}"
            const val ARG_ANIME_ID = "animeId"
            const val ARG_MAL_ID = "malId"
        }
    }

    data class SeasonDetail(val seasonId: Long = 0, val malId: Int = 0) : Route {
        override val route: String
            get() = if (malId > 0) "season_detail/$seasonId?malId=$malId" else "season_detail/$seasonId"

        companion object {
            const val ROUTE_PATTERN = "season_detail/{seasonId}?malId={malId}"
            const val ARG_SEASON_ID = "seasonId"
            const val ARG_MAL_ID = "malId"
        }
    }
}
