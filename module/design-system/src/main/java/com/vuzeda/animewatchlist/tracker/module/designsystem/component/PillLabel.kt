package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SmallSpacing

@Composable
fun PillLabel(
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
            modifier = Modifier.padding(horizontal = ElementSpacing, vertical = SmallSpacing),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PillLabelPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        PillLabel(label = "Filler", color = MaterialTheme.colorScheme.tertiary)
    }
}
