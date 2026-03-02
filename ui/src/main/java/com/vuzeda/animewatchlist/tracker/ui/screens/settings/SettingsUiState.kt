package com.vuzeda.animewatchlist.tracker.ui.screens.settings

import com.vuzeda.animewatchlist.tracker.domain.model.HomeViewMode
import com.vuzeda.animewatchlist.tracker.domain.model.TitleLanguage

data class SettingsUiState(
    val titleLanguage: TitleLanguage = TitleLanguage.DEFAULT,
    val homeViewMode: HomeViewMode = HomeViewMode.ANIME,
    val isDeleteConfirmationVisible: Boolean = false,
    val isDataDeleted: Boolean = false
)
