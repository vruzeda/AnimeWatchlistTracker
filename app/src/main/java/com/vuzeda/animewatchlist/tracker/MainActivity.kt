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
import com.vuzeda.animewatchlist.tracker.module.notification.android.NotificationHelper
import com.vuzeda.animewatchlist.tracker.module.scheduler.AnimeUpdateScheduler
import com.vuzeda.animewatchlist.tracker.module.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var animeUpdateScheduler: AnimeUpdateScheduler

    private var seasonMalId by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seasonMalId = intent.getIntExtra(NotificationHelper.EXTRA_SEASON_MAL_ID, 0)
        animeUpdateScheduler.scheduleAiringSeasonBackfill()
        enableEdgeToEdge()
        setContent {
            AnimeWatchlistTrackerTheme {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    seasonMalId = seasonMalId,
                    versionName = BuildConfig.VERSION_NAME,
                    versionCode = BuildConfig.VERSION_CODE
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        seasonMalId = intent.getIntExtra(NotificationHelper.EXTRA_SEASON_MAL_ID, 0)
    }
}
