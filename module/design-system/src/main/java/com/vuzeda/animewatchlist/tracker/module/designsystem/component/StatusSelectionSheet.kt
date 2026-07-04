package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.MinTouchTarget
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ScreenPadding
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SmallSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.extendedColors

data class StatusOption(val label: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelectionSheet(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    options: List<StatusOption>,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    SelectionModalBottomSheet(modifier = modifier, onDismiss = onDismiss) {
        StatusSelectionSheetContent(
            title = title,
            subtitle = subtitle,
            options = options,
            onOptionSelected = onOptionSelected
        )
    }
}

@Composable
fun StatusSelectionSheetContent(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    options: List<StatusOption>,
    onOptionSelected: (Int) -> Unit
) {
    SelectionSheetContent(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        itemCount = options.size
    ) { index ->
        val option = options[index]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
                .clickable { onOptionSelected(index) }
                .padding(horizontal = ScreenPadding, vertical = SmallSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ElementSpacing)
        ) {
            PillLabel(
                label = option.label,
                color = option.color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusSelectionSheetContentPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Surface {
            StatusSelectionSheetContent(
                title = "Add to watchlist",
                subtitle = "Attack on Titan: Final Season Part 3",
                options = listOf(
                    StatusOption("Watching", MaterialTheme.extendedColors.statusWatching),
                    StatusOption("Completed", MaterialTheme.extendedColors.statusCompleted),
                    StatusOption("Plan to Watch", MaterialTheme.extendedColors.statusPlanToWatch),
                    StatusOption("On Hold", MaterialTheme.extendedColors.statusOnHold),
                    StatusOption("Dropped", MaterialTheme.extendedColors.statusDropped)
                ),
                onOptionSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusSelectionSheetContentShortTitlePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Surface {
            StatusSelectionSheetContent(
                title = "Change status",
                subtitle = "Spy x Family",
                options = listOf(
                    StatusOption("Watching", MaterialTheme.extendedColors.statusWatching),
                    StatusOption("Completed", MaterialTheme.extendedColors.statusCompleted),
                    StatusOption("Plan to Watch", MaterialTheme.extendedColors.statusPlanToWatch),
                    StatusOption("On Hold", MaterialTheme.extendedColors.statusOnHold),
                    StatusOption("Dropped", MaterialTheme.extendedColors.statusDropped)
                ),
                onOptionSelected = {}
            )
        }
    }
}
