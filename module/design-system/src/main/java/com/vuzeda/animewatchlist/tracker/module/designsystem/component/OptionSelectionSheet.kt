package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.MinTouchTarget
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ScreenPadding
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SheetBottomPadding
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SmallSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSelectionSheet(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        OptionSelectionSheetContent(
            title = title,
            subtitle = subtitle,
            options = options,
            onOptionSelected = onOptionSelected
        )
    }
}

@Composable
fun OptionSelectionSheetContent(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = SheetBottomPadding)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = ElementSpacing)
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ScreenPadding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(ElementSpacing))

        options.forEachIndexed { index, option ->
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinTouchTarget)
                    .clickable { onOptionSelected(index) }
                    .padding(horizontal = ScreenPadding, vertical = SmallSpacing)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionSelectionSheetContentPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Surface {
            OptionSelectionSheetContent(
                title = "Enable notifications",
                subtitle = "Attack on Titan",
                options = listOf("New episodes only", "New seasons only", "Both"),
                onOptionSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionSelectionSheetContentAddScopePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        Surface {
            OptionSelectionSheetContent(
                title = "Add to watchlist",
                subtitle = "Spy x Family",
                options = listOf("All seasons", "First season only"),
                onOptionSelected = {}
            )
        }
    }
}
