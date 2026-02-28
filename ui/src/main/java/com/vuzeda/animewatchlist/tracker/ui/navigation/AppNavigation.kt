package com.vuzeda.animewatchlist.tracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vuzeda.animewatchlist.tracker.ui.screens.detail.DetailScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.home.HomeScreenRoute
import com.vuzeda.animewatchlist.tracker.ui.screens.search.SearchScreenRoute

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val BottomNavItems = listOf(
    BottomNavItem(label = "Home", icon = Icons.Default.Home, route = Route.Home.route),
    BottomNavItem(label = "Search", icon = Icons.Default.Search, route = Route.Search.route)
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
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
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
                        navController.navigate(Route.Detail(animeId).route)
                    }
                )
            }

            composable(Route.Search.route) {
                SearchScreenRoute(
                    onNavigateToDetail = { animeId ->
                        navController.navigate(Route.Detail(animeId = animeId).route)
                    },
                    onNavigateToDetailByMalId = { malId ->
                        navController.navigate(Route.Detail(malId = malId).route)
                    }
                )
            }

            composable(
                route = Route.Detail.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(Route.Detail.ARG_ANIME_ID) {
                        type = NavType.LongType
                    },
                    navArgument(Route.Detail.ARG_MAL_ID) {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) {
                DetailScreenRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
