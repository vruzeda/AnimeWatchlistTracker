package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme

@Composable
fun AnimeSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    placeholder: String = "Search anime...",
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Preview(showBackground = true)
@Composable
private fun AnimeSearchBarEmptyPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        AnimeSearchBar(
            modifier = Modifier.padding(16.dp),
            query = "",
            onQueryChanged = {},
            onSearch = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimeSearchBarWithQueryPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        AnimeSearchBar(
            modifier = Modifier.padding(16.dp),
            query = "Attack on Titan",
            onQueryChanged = {},
            onSearch = {}
        )
    }
}
