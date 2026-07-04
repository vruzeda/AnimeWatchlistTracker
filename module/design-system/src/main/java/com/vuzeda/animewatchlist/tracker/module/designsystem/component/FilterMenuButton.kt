package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
    DropdownIconMenu(
        modifier = modifier,
        isActive = selectedIndex != 0,
        icon = Icons.Default.FilterList,
        contentDescription = stringResource(R.string.cd_filter)
    ) { dismiss ->
        options.forEachIndexed { index, label ->
            DropdownMenuItem(
                modifier = Modifier.semantics { selected = index == selectedIndex },
                text = { Text(label) },
                onClick = {
                    onOptionSelected(index)
                    dismiss()
                },
                trailingIcon = if (index == selectedIndex) {
                    { SelectedCheckIcon() }
                } else {
                    null
                }
            )
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
