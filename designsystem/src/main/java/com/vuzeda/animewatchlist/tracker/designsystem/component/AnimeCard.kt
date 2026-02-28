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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusWatching

@Composable
fun AnimeCard(
    modifier: Modifier = Modifier,
    title: String,
    imageUrl: String?,
    statusLabel: String,
    statusColor: Color,
    currentEpisode: Int,
    totalEpisodes: Int?,
    score: Double?,
    onClick: () -> Unit
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

                Spacer(modifier = Modifier.height(4.dp))

                StatusChip(
                    label = statusLabel,
                    color = statusColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                val episodeText = if (totalEpisodes != null) {
                    "$currentEpisode / $totalEpisodes ep"
                } else {
                    "$currentEpisode ep"
                }
                Text(
                    text = episodeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (totalEpisodes != null && totalEpisodes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (currentEpisode.toFloat() / totalEpisodes).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimeCardPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        AnimeCard(
            modifier = Modifier.padding(16.dp),
            title = "Attack on Titan: Final Season Part 3",
            imageUrl = null,
            statusLabel = "Watching",
            statusColor = StatusWatching,
            currentEpisode = 10,
            totalEpisodes = 25,
            score = 9.1,
            onClick = {}
        )
    }
}
