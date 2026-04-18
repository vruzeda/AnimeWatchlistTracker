package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vuzeda.animewatchlist.tracker.module.designsystem.component.ConfirmationDialog
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ScreenPadding
import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.ui.R

private enum class TitleLanguageOption(
    val language: TitleLanguage,
    @StringRes val labelRes: Int
) {
    DEFAULT(TitleLanguage.DEFAULT, R.string.settings_title_language_default),
    ENGLISH(TitleLanguage.ENGLISH, R.string.settings_title_language_english),
    JAPANESE(TitleLanguage.JAPANESE, R.string.settings_title_language_japanese)
}

private enum class HomeViewModeOption(
    val mode: HomeViewMode,
    @StringRes val labelRes: Int
) {
    ANIME(HomeViewMode.ANIME, R.string.settings_home_view_mode_anime),
    SEASON(HomeViewMode.SEASON, R.string.settings_home_view_mode_season)
}

@Composable
fun SettingsScreenRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    onDeveloperClick: () -> Unit = {},
    versionName: String = "",
    versionCode: Int = 0
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feedbackUiState by feedbackViewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        feedbackUiState = feedbackUiState,
        onTitleLanguageSelected = viewModel::setTitleLanguage,
        onHomeViewModeSelected = viewModel::setHomeViewMode,
        onDeleteAllClick = viewModel::requestDeleteAllData,
        onConfirmDelete = viewModel::confirmDeleteAllData,
        onDismissDelete = viewModel::dismissDeleteConfirmation,
        onDataDeletedShown = viewModel::clearDataDeletedFlag,
        onFeedbackClick = viewModel::showFeedbackSheet,
        onFeedbackDismiss = {
            viewModel.hideFeedbackSheet()
            feedbackViewModel.reset()
        },
        onFeedbackCategorySelected = feedbackViewModel::selectCategory,
        onFeedbackMessageChanged = feedbackViewModel::updateMessage,
        onFeedbackSubmit = feedbackViewModel::submitFeedback,
        onFeedbackEventConsumed = feedbackViewModel::clearSnackbarEvent,
        onDeveloperClick = onDeveloperClick,
        versionName = versionName,
        versionCode = versionCode,
        onVersionTap = viewModel::onVersionTap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    feedbackUiState: FeedbackUiState = FeedbackUiState(),
    onTitleLanguageSelected: (TitleLanguage) -> Unit,
    onHomeViewModeSelected: (HomeViewMode) -> Unit,
    onDeleteAllClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onDataDeletedShown: () -> Unit,
    onFeedbackClick: () -> Unit = {},
    onFeedbackDismiss: () -> Unit = {},
    onFeedbackCategorySelected: (String) -> Unit = {},
    onFeedbackMessageChanged: (String) -> Unit = {},
    onFeedbackSubmit: () -> Unit = {},
    onFeedbackEventConsumed: () -> Unit = {},
    onDeveloperClick: () -> Unit = {},
    versionName: String = "",
    versionCode: Int = 0,
    onVersionTap: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val dataDeletedMessage = stringResource(R.string.settings_data_deleted)
    val context = LocalContext.current
    val developerOptionsEnabledMessage = stringResource(R.string.settings_developer_options_enabled)
    var activeToast by remember { mutableStateOf<Toast?>(null) }

    LaunchedEffect(uiState.isDataDeleted) {
        if (uiState.isDataDeleted) {
            snackbarHostState.showSnackbar(dataDeletedMessage)
            onDataDeletedShown()
        }
    }

    LaunchedEffect(uiState.developerTapCount) {
        val count = uiState.developerTapCount
        when {
            count >= 5 -> Toast.makeText(context, developerOptionsEnabledMessage, Toast.LENGTH_SHORT)
            count >= 3 -> {
                val remaining = 5 - count
                val message = context.resources.getQuantityString(
                    R.plurals.settings_developer_options_countdown,
                    remaining,
                    remaining
                )
                Toast.makeText(context, message, Toast.LENGTH_SHORT)
            }
            else -> null
        }?.also { toast ->
            activeToast?.cancel()
            activeToast = toast
            toast.show()
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )

            Text(
                text = stringResource(R.string.settings_title_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = ElementSpacing)
            )

            TitleLanguageOption.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onTitleLanguageSelected(option.language) }
                        .padding(horizontal = ScreenPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.titleLanguage == option.language,
                        onClick = null
                    )
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(ElementSpacing))
            HorizontalDivider(modifier = Modifier.padding(horizontal = ScreenPadding))
            Spacer(modifier = Modifier.height(ElementSpacing))

            Text(
                text = stringResource(R.string.settings_home_view_mode),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = ElementSpacing)
            )

            HomeViewModeOption.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onHomeViewModeSelected(option.mode) }
                        .padding(horizontal = ScreenPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.homeViewMode == option.mode,
                        onClick = null
                    )
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(ElementSpacing))
            HorizontalDivider(modifier = Modifier.padding(horizontal = ScreenPadding))
            Spacer(modifier = Modifier.height(ElementSpacing))

            TextButton(
                onClick = onDeleteAllClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.settings_delete_all_data),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(ElementSpacing))
            HorizontalDivider(modifier = Modifier.padding(horizontal = ScreenPadding))
            Spacer(modifier = Modifier.height(ElementSpacing))

            TextButton(
                onClick = onFeedbackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Feedback,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.feedback_button_label),
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.isDeveloperOptionsEnabled) {
                Spacer(modifier = Modifier.height(ElementSpacing))
                HorizontalDivider(modifier = Modifier.padding(horizontal = ScreenPadding))
                Spacer(modifier = Modifier.height(ElementSpacing))

                TextButton(
                    onClick = onDeveloperClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding)
                ) {
                    Text(
                        text = stringResource(R.string.developer_title),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.settings_version, versionName, versionCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onVersionTap)
                    .padding(horizontal = ScreenPadding, vertical = 16.dp)
            )
        }

        if (uiState.isDeleteConfirmationVisible) {
            ConfirmationDialog(
                title = stringResource(R.string.settings_delete_dialog_title),
                message = stringResource(R.string.settings_delete_dialog_message),
                confirmText = stringResource(R.string.settings_delete_dialog_confirm),
                dismissText = stringResource(R.string.settings_delete_dialog_dismiss),
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }

    if (uiState.isFeedbackSheetVisible) {
        FeedbackSheet(
            uiState = feedbackUiState,
            onCategorySelected = onFeedbackCategorySelected,
            onMessageChanged = onFeedbackMessageChanged,
            onSubmit = onFeedbackSubmit,
            onEventConsumed = onFeedbackEventConsumed,
            onDismiss = onFeedbackDismiss
        )
    }
}
