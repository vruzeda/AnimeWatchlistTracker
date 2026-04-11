package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import com.vuzeda.animewatchlist.tracker.module.domain.HomeViewMode
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage

data class SettingsUiState(
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val homeViewMode: HomeViewMode = HomeViewMode.ANIME,
    val isDeleteConfirmationVisible: Boolean = false,
    val isDataDeleted: Boolean = false,
    val developerTapCount: Int = 0,
    val isDeveloperOptionsEnabled: Boolean = false,
    val isFeedbackSheetVisible: Boolean = false
)
