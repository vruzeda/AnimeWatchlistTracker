package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.designsystem.R
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme

data class FilterGroup(
    val label: String,
    val options: List<String>,
    val selectedIndex: Int
)

@Composable
fun NestedFilterMenuButton(
    modifier: Modifier = Modifier,
    filterGroups: List<FilterGroup>,
    onOptionSelected: (groupIndex: Int, optionIndex: Int) -> Unit,
    resetLabel: String,
    onReset: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.cd_filter)
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            filterGroups.forEachIndexed { groupIndex, group ->
                if (groupIndex > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {},
                    enabled = false
                )

                group.options.forEachIndexed { optionIndex, label ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        },
                        onClick = { onOptionSelected(groupIndex, optionIndex) },
                        trailingIcon = if (optionIndex == group.selectedIndex) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            DropdownMenuItem(
                text = { Text(resetLabel) },
                onClick = { onReset() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NestedFilterMenuButtonPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        NestedFilterMenuButton(
            modifier = Modifier.padding(16.dp),
            filterGroups = listOf(
                FilterGroup(
                    label = "By Status",
                    options = listOf("All", "Watching", "Completed", "Plan to Watch", "On Hold", "Dropped"),
                    selectedIndex = 0
                ),
                FilterGroup(
                    label = "By Notification",
                    options = listOf("All", "On", "Off"),
                    selectedIndex = 0
                )
            ),
            onOptionSelected = { _, _ -> },
            resetLabel = "Reset Filters",
            onReset = {}
        )
    }
}
