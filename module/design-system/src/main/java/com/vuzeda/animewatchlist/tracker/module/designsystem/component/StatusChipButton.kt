package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.MinTouchTarget
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.extendedColors

@Composable
fun StatusChipButton(
    modifier: Modifier = Modifier,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            .wrapContentWidth()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        StatusChip(label = label, color = color)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusChipButtonPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Row(
            modifier = Modifier.padding(ElementSpacing),
            horizontalArrangement = Arrangement.spacedBy(ElementSpacing)
        ) {
            StatusChipButton(label = "Watching", color = MaterialTheme.extendedColors.statusWatching, onClick = {})
            StatusChipButton(label = "Completed", color = MaterialTheme.extendedColors.statusCompleted, onClick = {})
            StatusChipButton(label = "Plan to Watch", color = MaterialTheme.extendedColors.statusPlanToWatch, onClick = {})
            StatusChipButton(label = "On Hold", color = MaterialTheme.extendedColors.statusOnHold, onClick = {})
            StatusChipButton(label = "Dropped", color = MaterialTheme.extendedColors.statusDropped, onClick = {})
        }
    }
}
