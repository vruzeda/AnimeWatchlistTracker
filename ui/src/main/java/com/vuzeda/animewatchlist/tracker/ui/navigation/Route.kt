package com.vuzeda.animewatchlist.tracker.ui.navigation

sealed interface Route {
    val route: String

    data object Home : Route {
        override val route = "home"
    }

    data object Search : Route {
        override val route = "search"
    }

    data class Detail(val animeId: Long = 0, val malId: Int = 0) : Route {
        override val route: String
            get() = if (malId > 0) "detail/$animeId?malId=$malId" else "detail/$animeId"

        companion object {
            const val ROUTE_PATTERN = "detail/{animeId}?malId={malId}"
            const val ARG_ANIME_ID = "animeId"
            const val ARG_MAL_ID = "malId"
        }
    }
}
