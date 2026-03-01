package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.designsystem.R
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun EpisodeListItem(
    modifier: Modifier = Modifier,
    episodeNumber: Int,
    title: String?,
    airedDate: String?,
    isFiller: Boolean = false,
    isRecap: Boolean = false,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = stringResource(R.string.episode_number_format, episodeNumber),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title ?: stringResource(R.string.episode_title_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (airedDate != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = airedDate.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isFiller || isRecap) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isFiller) {
                        StatusChip(
                            label = stringResource(R.string.episode_badge_filler),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (isRecap) {
                        StatusChip(
                            label = stringResource(R.string.episode_badge_recap),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EpisodeListItemPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Surface {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                EpisodeListItem(
                    episodeNumber = 1,
                    title = "To You, in 2000 Years: The Fall of Shiganshina, Part 1",
                    airedDate = "2013-04-07T00:00:00+00:00"
                )
                EpisodeListItem(
                    episodeNumber = 2,
                    title = "That Day: The Fall of Shiganshina, Part 2",
                    airedDate = "2013-04-14T00:00:00+00:00",
                    isFiller = true
                )
                EpisodeListItem(
                    episodeNumber = 3,
                    title = "A Dim Light Amid Despair: Humanity's Comeback, Part 1",
                    airedDate = "2013-04-21T00:00:00+00:00",
                    isRecap = true,
                    showDivider = false
                )
            }
        }
    }
}
