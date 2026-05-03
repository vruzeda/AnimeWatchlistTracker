package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.MinTouchTarget
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusWatching

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
            StatusChipButton(label = "Watching", color = StatusWatching, onClick = {})
            StatusChipButton(label = "Completed", color = StatusCompleted, onClick = {})
            StatusChipButton(label = "Plan to Watch", color = StatusPlanToWatch, onClick = {})
            StatusChipButton(label = "On Hold", color = StatusOnHold, onClick = {})
            StatusChipButton(label = "Dropped", color = StatusDropped, onClick = {})
        }
    }
}
