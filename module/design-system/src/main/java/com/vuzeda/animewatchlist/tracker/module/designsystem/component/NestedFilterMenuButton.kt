package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SectionSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SmallSpacing

data class FilterGroup(
    val label: String,
    val options: List<String>,
    val selectedIndices: Set<Int>
)

@Composable
fun NestedFilterMenuButton(
    modifier: Modifier = Modifier,
    filterGroups: List<FilterGroup>,
    onOptionSelected: (groupIndex: Int, optionIndex: Int) -> Unit,
    resetLabel: String,
    onReset: () -> Unit
) {
    DropdownIconMenu(
        modifier = modifier,
        isActive = filterGroups.any { it.selectedIndices != setOf(0) },
        icon = Icons.Default.FilterList,
        contentDescription = stringResource(R.string.cd_filter)
    ) {
        filterGroups.forEachIndexed { groupIndex, group ->
            if (groupIndex > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = SmallSpacing))
            }

            GroupHeaderMenuItem(group.label)

            group.options.forEachIndexed { optionIndex, label ->
                DropdownMenuItem(
                    modifier = Modifier.semantics { selected = optionIndex in group.selectedIndices },
                    text = {
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = ElementSpacing)
                        )
                    },
                    onClick = { onOptionSelected(groupIndex, optionIndex) },
                    trailingIcon = if (optionIndex in group.selectedIndices) {
                        { SelectedCheckIcon() }
                    } else {
                        null
                    }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = SmallSpacing))

        ResetMenuItem(resetLabel, onReset)
    }
}

@Preview(showBackground = true, name = "Inactive")
@Composable
private fun NestedFilterMenuButtonInactivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        NestedFilterMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            filterGroups = listOf(
                FilterGroup(
                    label = "By Status",
                    options = listOf("All", "Watching", "Completed", "Plan to Watch", "On Hold", "Dropped"),
                    selectedIndices = setOf(0)
                ),
                FilterGroup(
                    label = "By Notification",
                    options = listOf("All", "On", "Off"),
                    selectedIndices = setOf(0)
                )
            ),
            onOptionSelected = { _, _ -> },
            resetLabel = "Reset Filters",
            onReset = {}
        )
    }
}

@Preview(showBackground = true, name = "Active")
@Composable
private fun NestedFilterMenuButtonActivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        NestedFilterMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            filterGroups = listOf(
                FilterGroup(
                    label = "By Status",
                    options = listOf("All", "Watching", "Completed", "Plan to Watch", "On Hold", "Dropped"),
                    selectedIndices = setOf(1, 2)
                ),
                FilterGroup(
                    label = "By Notification",
                    options = listOf("All", "On", "Off"),
                    selectedIndices = setOf(0)
                )
            ),
            onOptionSelected = { _, _ -> },
            resetLabel = "Reset Filters",
            onReset = {}
        )
    }
}
