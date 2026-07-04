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

@Composable
fun MultiSelectFilterMenuButton(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionToggled: (String) -> Unit,
    resetLabel: String,
    onReset: () -> Unit
) {
    DropdownIconMenu(
        modifier = modifier,
        isActive = selectedOptions.isNotEmpty(),
        icon = Icons.Default.FilterList,
        contentDescription = stringResource(R.string.cd_filter)
    ) {
        GroupHeaderMenuItem(label)

        options.forEach { option ->
            val isSelected = option in selectedOptions
            DropdownMenuItem(
                modifier = Modifier.semantics { selected = isSelected },
                text = {
                    Text(
                        text = option,
                        modifier = Modifier.padding(start = ElementSpacing)
                    )
                },
                onClick = { onOptionToggled(option) },
                trailingIcon = if (isSelected) {
                    { SelectedCheckIcon() }
                } else {
                    null
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = SmallSpacing))

        ResetMenuItem(resetLabel, onReset)
    }
}

@Preview(showBackground = true, name = "Inactive")
@Composable
private fun MultiSelectFilterMenuButtonInactivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        MultiSelectFilterMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            label = "By Type",
            options = listOf("TV", "OVA", "Movie", "Special", "ONA"),
            selectedOptions = emptySet(),
            onOptionToggled = {},
            resetLabel = "Reset Filters",
            onReset = {}
        )
    }
}

@Preview(showBackground = true, name = "Active")
@Composable
private fun MultiSelectFilterMenuButtonActivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        MultiSelectFilterMenuButton(
            modifier = Modifier.padding(SectionSpacing),
            label = "By Type",
            options = listOf("TV", "OVA", "Movie", "Special", "ONA"),
            selectedOptions = setOf("TV", "OVA"),
            onOptionToggled = {},
            resetLabel = "Reset Filters",
            onReset = {}
        )
    }
}
