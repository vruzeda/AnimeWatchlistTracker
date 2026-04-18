package com.vuzeda.animewatchlist.tracker.module.designsystem.component

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
import androidx.compose.ui.text.font.FontWeight
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
    var isExpanded by remember { mutableStateOf(false) }
    val isActive = selectedOptions.isNotEmpty()

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
            DropdownMenuItem(
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {},
                enabled = false
            )

            options.forEach { option ->
                val isSelected = option in selectedOptions
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = ElementSpacing)
                        )
                    },
                    onClick = { onOptionToggled(option) },
                    trailingIcon = if (isSelected) {
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

            HorizontalDivider(modifier = Modifier.padding(vertical = SmallSpacing))

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
private fun MultiSelectFilterMenuButtonPreview() {
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
