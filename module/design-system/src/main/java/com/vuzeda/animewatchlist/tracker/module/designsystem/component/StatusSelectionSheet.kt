package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.StatusWatching

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
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(index) }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(
                    label = option.label,
                    color = option.color
                )
            }
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
                    StatusOption("Watching", StatusWatching),
                    StatusOption("Completed", StatusCompleted),
                    StatusOption("Plan to Watch", StatusPlanToWatch),
                    StatusOption("On Hold", StatusOnHold),
                    StatusOption("Dropped", StatusDropped)
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
                    StatusOption("Watching", StatusWatching),
                    StatusOption("Completed", StatusCompleted),
                    StatusOption("Plan to Watch", StatusPlanToWatch),
                    StatusOption("On Hold", StatusOnHold),
                    StatusOption("Dropped", StatusDropped)
                ),
                onOptionSelected = {}
            )
        }
    }
}
