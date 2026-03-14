package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun EpisodeStepper(
    modifier: Modifier = Modifier,
    currentEpisode: Int,
    totalEpisodes: Int?,
    onEpisodeChanged: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = { onEpisodeChanged(currentEpisode - 1) }) {
            Text(stringResource(R.string.episode_stepper_decrement))
        }

        val episodeText = if (totalEpisodes != null) {
            stringResource(R.string.episode_stepper_with_total, currentEpisode, totalEpisodes)
        } else {
            stringResource(R.string.episode_stepper_without_total, currentEpisode)
        }
        Text(
            text = episodeText,
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedButton(onClick = { onEpisodeChanged(currentEpisode + 1) }) {
            Text(stringResource(R.string.episode_stepper_increment))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EpisodeStepperWithTotalPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        EpisodeStepper(
            modifier = Modifier.padding(16.dp),
            currentEpisode = 5,
            totalEpisodes = 24,
            onEpisodeChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EpisodeStepperWithoutTotalPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        EpisodeStepper(
            modifier = Modifier.padding(16.dp),
            currentEpisode = 12,
            totalEpisodes = null,
            onEpisodeChanged = {}
        )
    }
}
