package com.vuzeda.animewatchlist.tracker.module.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.LocalNavAnimatedVisibilityScope
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.LocalSharedTransitionScope
import com.vuzeda.animewatchlist.tracker.module.ui.R
import com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail.AnimeDetailScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.developer.DeveloperScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.HomeScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule.ScheduleScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.search.SearchScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail.SeasonDetailScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.seasons.SeasonsScreenRoute
import com.vuzeda.animewatchlist.tracker.module.ui.screens.settings.SettingsScreenRoute
import kotlin.reflect.KClass

private data class BottomNavItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: Route,
    val routeClass: KClass<out Route>
)

private val BottomNavItems = listOf(
    BottomNavItem(labelRes = R.string.nav_home, icon = Icons.Default.Home, route = Route.Home, routeClass = Route.Home::class),
    BottomNavItem(labelRes = R.string.nav_seasons, icon = Icons.Default.DateRange, route = Route.Seasons, routeClass = Route.Seasons::class),
    BottomNavItem(labelRes = R.string.nav_search, icon = Icons.Default.Search, route = Route.Search, routeClass = Route.Search::class),
    BottomNavItem(labelRes = R.string.nav_settings, icon = Icons.Default.Settings, route = Route.Settings, routeClass = Route.Settings::class)
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    seasonMalId: Int = 0,
    versionName: String = "",
    versionCode: Int = 0
) {
    val navController = rememberNavController()

    LaunchedEffect(seasonMalId) {
        if (seasonMalId > 0) {
            navController.navigate(Route.SeasonDetail(malId = seasonMalId))
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isBottomBarVisible = BottomNavItems.any { item ->
        currentDestination?.hasRoute(item.routeClass) == true
    }

    SharedTransitionLayout(modifier = modifier) {
        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    NavigationBar {
                        BottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(imageVector = item.icon, contentDescription = stringResource(item.labelRes)) },
                                label = { Text(stringResource(item.labelRes)) },
                                selected = currentDestination?.hasRoute(item.routeClass) == true,
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
                startDestination = Route.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<Route.Home> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        HomeScreenRoute(
                            onAnimeClick = { animeId ->
                                navController.navigate(Route.AnimeDetail(animeId = animeId))
                            },
                            onSeasonClick = { seasonId ->
                                navController.navigate(Route.SeasonDetail(seasonId = seasonId))
                            },
                            onScheduleClick = {
                                navController.navigate(Route.Schedule)
                            }
                        )
                    }
                }

                composable<Route.Schedule> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        ScheduleScreenRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onSeasonClick = { seasonId ->
                                navController.navigate(Route.SeasonDetail(seasonId = seasonId))
                            }
                        )
                    }
                }

                composable<Route.Seasons> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        SeasonsScreenRoute(
                            onNavigateToDetailByMalId = { malId ->
                                navController.navigate(Route.SeasonDetail(malId = malId))
                            }
                        )
                    }
                }

                composable<Route.Settings> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        SettingsScreenRoute(
                            onDeveloperClick = { navController.navigate(Route.Developer) },
                            versionName = versionName,
                            versionCode = versionCode
                        )
                    }
                }

                composable<Route.Developer> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        DeveloperScreenRoute(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable<Route.Search> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        SearchScreenRoute(
                            onNavigateToSeasonDetailByMalId = { malId ->
                                navController.navigate(Route.SeasonDetail(malId = malId))
                            }
                        )
                    }
                }

                composable<Route.AnimeDetail> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        AnimeDetailScreenRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onSeasonClick = { seasonId, malId ->
                                if (seasonId > 0) {
                                    navController.navigate(Route.SeasonDetail(seasonId = seasonId))
                                } else {
                                    navController.navigate(Route.SeasonDetail(malId = malId))
                                }
                            }
                        )
                    }
                }

                composable<Route.SeasonDetail> {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        SeasonDetailScreenRoute(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAnimeDetail = { malId ->
                                navController.navigate(Route.AnimeDetail(malId = malId))
                            }
                        )
                    }
                }
            }
        }
    }
}
