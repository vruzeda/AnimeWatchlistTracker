package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.MinTouchTarget
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ScreenPadding

@Composable
fun SettingsSwitchRow(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    supporting: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = ScreenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsSwitchRowPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        SettingsSwitchRow(
            label = "Cache covers offline",
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsSwitchRowWithSupportingPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        SettingsSwitchRow(
            label = "Cache covers offline",
            supporting = "12.4 MB used",
            checked = false,
            onCheckedChange = {},
        )
    }
}
