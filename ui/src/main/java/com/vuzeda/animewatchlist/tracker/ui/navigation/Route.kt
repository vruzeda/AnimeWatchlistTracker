package com.vuzeda.animewatchlist.tracker.ui.navigation

sealed interface Route {
    val route: String

    data object Home : Route {
        override val route = "home"
    }

    data object Search : Route {
        override val route = "search"
    }

    data class Detail(val animeId: Long) : Route {
        override val route = "detail/$animeId"

        companion object {
            const val ROUTE_PATTERN = "detail/{animeId}"
            const val ARG_ANIME_ID = "animeId"
        }
    }
}
