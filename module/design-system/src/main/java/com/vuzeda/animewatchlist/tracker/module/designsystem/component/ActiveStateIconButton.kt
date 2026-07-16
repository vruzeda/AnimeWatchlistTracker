package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SectionSpacing

@Composable
fun ActiveStateIconButton(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    if (isActive) {
        FilledIconButton(modifier = modifier, onClick = onClick) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    } else {
        IconButton(modifier = modifier, onClick = onClick) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Preview(showBackground = true, name = "Inactive")
@Composable
private fun ActiveStateIconButtonInactivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Row(modifier = Modifier.padding(SectionSpacing)) {
            ActiveStateIconButton(
                isActive = false,
                onClick = {},
                icon = Icons.Default.FilterList,
                contentDescription = "Filter"
            )
        }
    }
}

@Preview(showBackground = true, name = "Active")
@Composable
private fun ActiveStateIconButtonActivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Row(modifier = Modifier.padding(SectionSpacing)) {
            ActiveStateIconButton(
                isActive = true,
                onClick = {},
                icon = Icons.Default.FilterList,
                contentDescription = "Filter"
            )
        }
    }
}
