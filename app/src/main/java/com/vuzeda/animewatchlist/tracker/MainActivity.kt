package com.vuzeda.animewatchlist.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.ui.navigation.AppNavigation
import com.vuzeda.animewatchlist.tracker.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private var seasonMalId by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seasonMalId = intent.getIntExtra(EXTRA_SEASON_MAL_ID, 0)
        enableEdgeToEdge()
        setContent {
            AnimeWatchlistTrackerTheme {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    seasonMalId = seasonMalId,
                    onFireTestEpisodeNotification = if (BuildConfig.DEBUG) {
                        {
                            notificationHelper.showUpdateNotification(
                                AnimeUpdate.NewEpisodes(
                                    anime = Anime(title = "Fullmetal Alchemist"),
                                    season = Season(malId = 121, title = "Fullmetal Alchemist"),
                                    newEpisodeCount = 3
                                )
                            )
                        }
                    } else {
                        {}
                    },
                    onFireTestSeasonNotification = if (BuildConfig.DEBUG) {
                        {
                            notificationHelper.showUpdateNotification(
                                AnimeUpdate.NewSeason(
                                    anime = Anime(title = "Fullmetal Alchemist"),
                                    sequelMalId = 430,
                                    sequelTitle = "Fullmetal Alchemist: The Conqueror of Shamballa"
                                )
                            )
                        }
                    } else {
                        {}
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        seasonMalId = intent.getIntExtra(EXTRA_SEASON_MAL_ID, 0)
    }

    companion object {
        const val EXTRA_SEASON_MAL_ID = "extra_season_mal_id"
    }
}
