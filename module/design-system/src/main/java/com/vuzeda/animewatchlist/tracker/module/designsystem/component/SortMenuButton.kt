package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SectionSpacing

@Composable
fun SortMenuButton(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedIndex: Int,
    isAscending: Boolean = true,
    onOptionSelected: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isActive = selectedIndex != 0 || !isAscending

    Box(modifier = modifier) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.cd_sort),
                tint = if (isActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onOptionSelected(index)
                        isExpanded = false
                    },
                    trailingIcon = if (index == selectedIndex) {
                        {
                            Icon(
                                imageVector = if (isAscending) {
                                    Icons.Default.ArrowUpward
                                } else {
                                    Icons.Default.ArrowDownward
                                },
                                contentDescription = stringResource(
                                    if (isAscending) R.string.cd_ascending else R.string.cd_descending
                                ),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Inactive")
@Composable
private fun SortMenuButtonInactivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        SortMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            options = listOf("Alphabetical", "MAL Score", "Your Rating", "Progress"),
            selectedIndex = 0,
            isAscending = true,
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Active")
@Composable
private fun SortMenuButtonActivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        SortMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            options = listOf("Alphabetical", "MAL Score", "Your Rating", "Progress"),
            selectedIndex = 1,
            isAscending = true,
            onOptionSelected = {}
        )
    }
}
