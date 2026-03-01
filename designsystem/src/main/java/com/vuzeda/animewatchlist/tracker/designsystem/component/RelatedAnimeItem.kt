package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun RelatedAnimeItem(
    modifier: Modifier = Modifier,
    title: String,
    relationLabel: String,
    relationColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                StatusChip(label = relationLabel, color = relationColor)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RelatedAnimeItemPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RelatedAnimeItem(
                    title = "Attack on Titan Season 2",
                    relationLabel = "Sequel",
                    relationColor = MaterialTheme.colorScheme.primary,
                    onClick = {}
                )
                RelatedAnimeItem(
                    title = "Attack on Titan: No Regrets",
                    relationLabel = "Prequel",
                    relationColor = MaterialTheme.colorScheme.secondary,
                    onClick = {}
                )
            }
        }
    }
}
