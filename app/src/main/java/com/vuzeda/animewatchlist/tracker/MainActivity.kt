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
import com.vuzeda.animewatchlist.tracker.module.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                    versionName = BuildConfig.VERSION_NAME,
                    versionCode = BuildConfig.VERSION_CODE
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
