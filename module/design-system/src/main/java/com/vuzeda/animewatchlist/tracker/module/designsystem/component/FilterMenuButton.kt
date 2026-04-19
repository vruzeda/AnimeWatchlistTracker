package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
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
fun FilterMenuButton(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isActive = selectedIndex != 0

    Box(modifier = modifier) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.cd_filter),
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
    }
}

@Preview(showBackground = true, name = "Inactive")
@Composable
private fun FilterMenuButtonInactivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        FilterMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            options = listOf("All", "Watching", "Completed", "Plan to Watch", "On Hold", "Dropped"),
            selectedIndex = 0,
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Active")
@Composable
private fun FilterMenuButtonActivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        FilterMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            options = listOf("All", "Watching", "Completed", "Plan to Watch", "On Hold", "Dropped"),
            selectedIndex = 1,
            onOptionSelected = {}
        )
    }
}
