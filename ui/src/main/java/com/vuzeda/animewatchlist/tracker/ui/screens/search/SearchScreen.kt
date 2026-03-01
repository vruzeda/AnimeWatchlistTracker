package com.vuzeda.animewatchlist.tracker.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeCard
import com.vuzeda.animewatchlist.tracker.designsystem.component.AnimeSearchBar
import com.vuzeda.animewatchlist.tracker.designsystem.component.EmptyStateMessage
import com.vuzeda.animewatchlist.tracker.designsystem.component.SortMenuButton
import com.vuzeda.animewatchlist.tracker.domain.model.SearchResult
import com.vuzeda.animewatchlist.tracker.ui.R

@Composable
fun SearchScreenRoute(
    onNavigateToDetailByMalId: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.pendingNavigationMalId) {
        val malId = uiState.pendingNavigationMalId
        if (malId != null) {
            viewModel.onNavigated()
            onNavigateToDetailByMalId(malId)
        }
    }

    SearchScreen(
        uiState = uiState,
        onQueryChanged = viewModel::updateQuery,
        onSearch = viewModel::search,
        onResultClick = viewModel::onResultClick,
        onSortSelected = viewModel::selectSort,
        onSnackbarDismissed = viewModel::clearSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onResultClick: (SearchResult) -> Unit,
    onSortSelected: (SearchSortOption) -> Unit,
    onSnackbarDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val sortOptions = SearchSortOption.entries.map { stringResource(it.displayLabelRes) }

    val addedFormat = stringResource(R.string.search_added_to_watchlist, uiState.snackbarMessage ?: "")
    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            snackbarHostState.showSnackbar(addedFormat)
            onSnackbarDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    if (uiState.hasSearched && uiState.results.isNotEmpty()) {
                        SortMenuButton(
                            options = sortOptions,
                            selectedIndex = uiState.sortOption.ordinal,
                            isAscending = uiState.isSortAscending,
                            onOptionSelected = { index -> onSortSelected(SearchSortOption.entries[index]) }
                        )
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
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
                        title = stringResource(R.string.search_error_title),
                        subtitle = uiState.errorMessage
                    )
                }
                uiState.hasSearched && uiState.results.isEmpty() -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.search_no_results_title),
                        subtitle = stringResource(R.string.search_no_results_subtitle)
                    )
                }
                !uiState.hasSearched -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.Search,
                        title = stringResource(R.string.search_initial_title),
                        subtitle = stringResource(R.string.search_initial_subtitle)
                    )
                }
                uiState.displayedResults.isEmpty() -> {
                    EmptyStateMessage(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.search_no_filter_match_title),
                        subtitle = stringResource(R.string.search_no_filter_match_subtitle)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.displayedResults,
                            key = { it.malId }
                        ) { result ->
                            AnimeCard(
                                title = result.title,
                                imageUrl = result.imageUrl,
                                onClick = { onResultClick(result) },
                                score = result.score,
                                episodeText = result.episodeCount?.let { stringResource(R.string.search_episode_count, it) },
                                genresText = result.genres.takeIf { it.isNotEmpty() }?.joinToString(", ")
                            )
                        }
                    }
                }
            }
        }
    }
}
