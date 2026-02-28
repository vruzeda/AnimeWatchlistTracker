package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusCompleted
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusDropped
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusOnHold
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusPlanToWatch
import com.vuzeda.animewatchlist.tracker.designsystem.theme.StatusWatching

data class StatusOption(val label: String, val color: Color)

@Composable
fun StatusDropdownSelector(
    modifier: Modifier = Modifier,
    selectedLabel: String,
    options: List<StatusOption>,
    onOptionSelected: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { isExpanded = true }) {
            Text(selectedLabel)
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onOptionSelected(index)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusDropdownSelectorPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        StatusDropdownSelector(
            modifier = Modifier.padding(16.dp),
            selectedLabel = "Watching",
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
