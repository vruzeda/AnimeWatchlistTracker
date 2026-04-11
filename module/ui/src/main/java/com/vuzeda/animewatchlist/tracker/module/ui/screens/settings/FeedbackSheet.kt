package com.vuzeda.animewatchlist.tracker.module.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.module.domain.FeedbackCategory
import com.vuzeda.animewatchlist.tracker.module.ui.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackSheet(
    uiState: FeedbackUiState,
    onCategorySelected: (String) -> Unit,
    onMessageChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onEventConsumed: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.feedback_success)
    val errorMessage = stringResource(R.string.feedback_error)

    LaunchedEffect(uiState.snackbarEvent) {
        when (uiState.snackbarEvent) {
            FeedbackSnackbarEvent.Success -> {
                onEventConsumed()
                onDismiss()
            }
            FeedbackSnackbarEvent.Error -> {
                snackbarHostState.showSnackbar(errorMessage)
                onEventConsumed()
            }
            null -> Unit
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.feedback_sheet_title),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                val categories = listOf(
                    FeedbackCategory.BUG_REPORT to stringResource(R.string.feedback_category_bug),
                    FeedbackCategory.FEATURE_REQUEST to stringResource(R.string.feedback_category_feature),
                    FeedbackCategory.GENERAL to stringResource(R.string.feedback_category_general)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { (category, label) ->
                        FilterChip(
                            selected = uiState.category == category.name,
                            onClick = { onCategorySelected(category.name) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = onMessageChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.feedback_message_hint)) },
                    supportingText = {
                        Text(stringResource(R.string.feedback_char_count, uiState.charCount))
                    },
                    minLines = 4,
                    maxLines = 8
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSubmit,
                    enabled = uiState.isValid && !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.feedback_submit))
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
