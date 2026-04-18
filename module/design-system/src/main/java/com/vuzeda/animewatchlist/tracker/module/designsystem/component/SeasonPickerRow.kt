package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SectionSpacing

@Composable
fun SeasonPickerRow(
    modifier: Modifier = Modifier,
    label: String,
    isPreviousEnabled: Boolean = true,
    isNextEnabled: Boolean = true,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousClick,
            enabled = isPreviousEnabled
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.cd_previous_season)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onNextClick,
            enabled = isNextEnabled
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.cd_next_season)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeasonPickerRowPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        SeasonPickerRow(
            modifier = Modifier.padding(SectionSpacing),
            label = "Winter 2026",
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SeasonPickerRowNextDisabledPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        SeasonPickerRow(
            modifier = Modifier.padding(SectionSpacing),
            label = "Spring 2026",
            isNextEnabled = false,
            onPreviousClick = {},
            onNextClick = {}
        )
    }
}
