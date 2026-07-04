package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.extendedColors

@Composable
fun StatusChip(
    modifier: Modifier = Modifier,
    label: String,
    color: Color
) {
    PillLabel(modifier = modifier, label = label, color = color)
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Row(
            modifier = Modifier.padding(ElementSpacing),
            horizontalArrangement = Arrangement.spacedBy(ElementSpacing)
        ) {
            StatusChip(label = "Watching", color = MaterialTheme.extendedColors.statusWatching)
            StatusChip(label = "Completed", color = MaterialTheme.extendedColors.statusCompleted)
            StatusChip(label = "Plan to Watch", color = MaterialTheme.extendedColors.statusPlanToWatch)
            StatusChip(label = "On Hold", color = MaterialTheme.extendedColors.statusOnHold)
            StatusChip(label = "Dropped", color = MaterialTheme.extendedColors.statusDropped)
        }
    }
}
