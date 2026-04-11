package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

data class FeedbackUiState(
    val category: String? = null,
    val message: String = "",
    val isSubmitting: Boolean = false,
    val snackbarEvent: FeedbackSnackbarEvent? = null
) {
    val isValid: Boolean get() = category != null && message.length in 10..500
    val charCount: Int get() = message.length
}

sealed interface FeedbackSnackbarEvent {
    data object Success : FeedbackSnackbarEvent
    data object Error : FeedbackSnackbarEvent
}
