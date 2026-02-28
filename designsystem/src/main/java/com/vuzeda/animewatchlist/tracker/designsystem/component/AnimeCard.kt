package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusWatching

@Composable
fun AnimeCard(
    modifier: Modifier = Modifier,
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    statusLabel: String? = null,
    statusColor: Color = Color.Transparent,
    score: Double? = null,
    episodeText: String? = null,
    progress: Float? = null,
    genresText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .size(width = 72.dp, height = 100.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (statusLabel != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        label = statusLabel,
                        color = statusColor
                    )
                }

                if (score != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "★ $score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (episodeText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = episodeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (progress != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                    )
                }

                if (genresText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = genresText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimeCardWatchlistPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        AnimeCard(
            modifier = Modifier.padding(16.dp),
            title = "Attack on Titan: Final Season Part 3",
            imageUrl = null,
            statusLabel = "Watching",
            statusColor = StatusWatching,
            episodeText = "10 / 25 ep",
            progress = 0.4f,
            score = 9.1,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimeCardSearchResultPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        AnimeCard(
            modifier = Modifier.padding(16.dp),
            title = "Jujutsu Kaisen Season 2",
            imageUrl = null,
            score = 8.6,
            episodeText = "23 episodes",
            genresText = "Action, Fantasy, School",
            onClick = {},
            trailingContent = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to watchlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimeCardInWatchlistPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        AnimeCard(
            modifier = Modifier.padding(16.dp),
            title = "Spy x Family",
            imageUrl = null,
            score = 8.5,
            episodeText = "25 episodes",
            genresText = "Action, Comedy, Slice of Life",
            onClick = {},
            trailingContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Already in watchlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        label = "Plan to Watch",
                        color = StatusPlanToWatch
                    )
                }
            }
        )
    }
}
