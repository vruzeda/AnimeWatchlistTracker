package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun DropdownIconMenu(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    icon: ImageVector,
    contentDescription: String,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ActiveStateIconButton(
            isActive = isActive,
            onClick = { isExpanded = true },
            icon = icon,
            contentDescription = contentDescription
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            scrollState = rememberScrollState()
        ) {
            content { isExpanded = false }
        }
    }
}

@Composable
internal fun SelectedCheckIcon() {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun ColumnScope.GroupHeaderMenuItem(label: String) {
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
}

@Composable
internal fun ColumnScope.ResetMenuItem(resetLabel: String, onReset: () -> Unit) {
    DropdownMenuItem(
        text = { Text(resetLabel) },
        onClick = onReset,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
