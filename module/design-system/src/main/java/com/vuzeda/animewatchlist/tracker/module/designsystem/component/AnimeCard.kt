package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.CardThumbnailHeight
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.CardThumbnailWidth
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusWatching

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimeCard(
    modifier: Modifier = Modifier,
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    imageSharedElementKey: Any? = null,
    score: Double? = null,
    genresText: String? = null,
    episodeText: String? = null,
    progress: Float? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val sharedImageModifier: Modifier = if (
        imageSharedElementKey != null &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    ) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                rememberSharedContentState(imageSharedElementKey),
                animatedVisibilityScope
            )
        }
    } else Modifier

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
                modifier = sharedImageModifier
                    .size(width = CardThumbnailWidth, height = CardThumbnailHeight)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color(0xFFE0E0E0)),
                error = ColorPainter(Color(0xFFE0E0E0)),
                fallback = ColorPainter(Color(0xFFE0E0E0))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (score != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.score_format, score.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            score = 9.1,
            genresText = "Action, Drama, Fantasy",
            episodeText = "10 / 25 ep",
            progress = 0.4f,
            onClick = {},
            trailingContent = {
                StatusChip(
                    label = "Watching",
                    color = StatusWatching
                )
            }
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
            genresText = "Action, Fantasy, School",
            episodeText = "23 episodes",
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
            genresText = "Action, Comedy, Slice of Life",
            episodeText = "25 episodes",
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
