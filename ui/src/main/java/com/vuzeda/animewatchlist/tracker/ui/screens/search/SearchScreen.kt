package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeSearchBar
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.domain.model.Anime
import com.vuzeda.animewatchlist.tracker.domain.model.WatchStatus
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toColor
import com.vuzeda.animewatchlist.tracker.ui.screens.home.toDisplayLabel

@Composable
fun SearchScreenRoute(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.pendingNavigationId) {
        val id = uiState.pendingNavigationId
        if (id != null) {
            viewModel.onNavigated()
            onNavigateToDetail(id)
        }
    }

    SearchScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onSearch = viewModel::search,
        onAnimeClick = viewModel::onAnimeClick,
        onAddClick = viewModel::onAddClick,
        onStatusSelected = viewModel::onStatusSelected,
        onDismissBottomSheet = viewModel::dismissBottomSheet,
        onSnackbarDismissed = viewModel::clearSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onAnimeClick: (Anime) -> Unit,
    onAddClick: (Anime) -> Unit,
    onStatusSelected: (WatchStatus) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onSnackbarDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            TopAppBar(title = { Text("Search Anime") })

            AnimeSearchBar(
                modifier = Modifier.padding(horizontal = 16.dp),
                query = uiState.query,
                onQueryChanged = onQueryChanged,
                onSearch = onSearch
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = "Something went wrong",
                        subtitle = uiState.errorMessage
                    )
                }
                uiState.hasSearched && uiState.results.isEmpty() -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = "No results found",
                        subtitle = "Try a different search term"
                    )
                }
                !uiState.hasSearched -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.Search,
                        title = "Search for anime",
                        subtitle = "Find anime from MyAnimeList and add them to your watchlist"
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.results,
                            key = { it.malId ?: it.hashCode() }
                        ) { anime ->
                            val isAdded = anime.malId in uiState.addedAnimeIds
                            SearchResultItem(
                                anime = anime,
                                isAdded = isAdded,
                                onClick = { onAnimeClick(anime) },
                                onAddClick = { onAddClick(anime) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.selectedAnimeForAdd != null) {
        StatusSelectionBottomSheet(
            animeTitle = uiState.selectedAnimeForAdd.title,
            onStatusSelected = onStatusSelected,
            onDismiss = onDismissBottomSheet
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelectionBottomSheet(
    animeTitle: String,
    onStatusSelected: (WatchStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Add to watchlist",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Text(
                text = animeTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            WatchStatus.entries.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStatusSelected(status) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    com.vuzeda.animewatchlist.tracker.designsystem.component.StatusChip(
                        label = status.toDisplayLabel(),
                        color = status.toColor()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    modifier: Modifier = Modifier,
    anime: Anime,
    isAdded: Boolean,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = anime.imageUrl,
                contentDescription = anime.title,
                modifier = Modifier
                    .size(width = 60.dp, height = 84.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (anime.score != null) {
                    Text(
                        text = "★ ${anime.score}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (anime.episodeCount != null) {
                    Text(
                        text = "${anime.episodeCount} episodes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (anime.genres.isNotEmpty()) {
                    Text(
                        text = anime.genres.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isAdded) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Already in watchlist",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to watchlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
