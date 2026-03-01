package com.vuzeda.animewatchlist.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MockMainActivity : ComponentActivity() {

    @Inject
    lateinit var screenshotSeeder: ScreenshotSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            screenshotSeeder.seedIfEmpty()
        }

        setContent {
            AnimeWatchlistTrackerTheme {
                AppNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
