package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusWatching

@Composable
fun StatusChip(
    modifier: Modifier = Modifier,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(label = "Watching", color = StatusWatching)
            StatusChip(label = "Completed", color = StatusCompleted)
            StatusChip(label = "Plan to Watch", color = StatusPlanToWatch)
            StatusChip(label = "On Hold", color = StatusOnHold)
            StatusChip(label = "Dropped", color = StatusDropped)
        }
    }
}
