package com.vuzeda.animewatchlist.tracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vuzeda.animewatchlist.tracker.ui.R
import com.vuzeda.animewatchlist.tracker.ui.screens.animedetail.AnimeDetailScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.home.HomeScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.search.SearchScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.seasondetail.SeasonDetailScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.seasons.SeasonsScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.settings.SettingsScreenRoute

private data class BottomNavItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: String
)

private val BottomNavItems = listOf(
    BottomNavItem(labelRes = R.string.nav_home, icon = Icons.Default.Home, route = Route.Home.route),
    BottomNavItem(labelRes = R.string.nav_seasons, icon = Icons.Default.DateRange, route = Route.Seasons.route),
    BottomNavItem(labelRes = R.string.nav_search, icon = Icons.Default.Search, route = Route.Search.route),
    BottomNavItem(labelRes = R.string.nav_settings, icon = Icons.Default.Settings, route = Route.Settings.route)
)

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isBottomBarVisible = currentDestination?.hierarchy?.any { dest ->
        BottomNavItems.any { it.route == dest.route }
    } == true

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    BottomNavItems.forEach { item ->
                    NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes)) },
                            selected = currentDestination.hierarchy.any { it.route == item.route },
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Home.route) {
                HomeScreenRoute(
                    onAnimeClick = { animeId ->
                        navController.navigate(Route.AnimeDetail(animeId).route)
                    }
                )
            }

            composable(Route.Seasons.route) {
                SeasonsScreenRoute(
                    onNavigateToDetailByMalId = { malId ->
                        navController.navigate(Route.AnimeDetail(malId = malId).route)
                    }
                )
            }

            composable(Route.Settings.route) {
                SettingsScreenRoute()
            }

            composable(Route.Search.route) {
                SearchScreenRoute(
                    onNavigateToDetailByMalId = { malId ->
                        navController.navigate(Route.AnimeDetail(malId = malId).route)
                    }
                )
            }

            composable(
                route = Route.AnimeDetail.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(Route.AnimeDetail.ARG_ANIME_ID) {
                        type = NavType.LongType
                    },
                    navArgument(Route.AnimeDetail.ARG_MAL_ID) {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) {
                AnimeDetailScreenRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onSeasonClick = { seasonId, malId ->
                        if (seasonId > 0) {
                            navController.navigate(Route.SeasonDetail(seasonId = seasonId).route)
                        } else {
                            navController.navigate(Route.SeasonDetail(malId = malId).route)
                        }
                    }
                )
            }

            composable(
                route = Route.SeasonDetail.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(Route.SeasonDetail.ARG_SEASON_ID) {
                        type = NavType.LongType
                    },
                    navArgument(Route.SeasonDetail.ARG_MAL_ID) {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) {
                SeasonDetailScreenRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
