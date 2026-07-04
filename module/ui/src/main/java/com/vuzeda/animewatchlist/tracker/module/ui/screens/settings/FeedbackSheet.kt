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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ScreenPadding
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SectionSpacing
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SheetBottomPadding
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SmallLoadingIndicatorSize
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.SubtleSpacing
import com.vuzeda.animewatchlist.tracker.module.domain.FeedbackCategory
import com.vuzeda.animewatchlist.tracker.module.ui.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackSheet(
    uiState: FeedbackUiState,
    onCategorySelected: (String) -> Unit,
    onMessageChanged: (String) -> Unit,
    onContactNameChanged: (String) -> Unit,
    onContactEmailChanged: (String) -> Unit,
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
                    .padding(horizontal = ScreenPadding)
                    .padding(bottom = SheetBottomPadding)
            ) {
                Text(
                    text = stringResource(R.string.feedback_sheet_title),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(ElementSpacing))

                val categories = listOf(
                    FeedbackCategory.BUG_REPORT to stringResource(R.string.feedback_category_bug),
                    FeedbackCategory.FEATURE_REQUEST to stringResource(R.string.feedback_category_feature),
                    FeedbackCategory.GENERAL to stringResource(R.string.feedback_category_general)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(ElementSpacing)) {
                    categories.forEach { (category, label) ->
                        FilterChip(
                            selected = uiState.category == category.name,
                            onClick = { onCategorySelected(category.name) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ElementSpacing))

                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = onMessageChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.feedback_message_label)) },
                    placeholder = { Text(stringResource(R.string.feedback_message_hint)) },
                    supportingText = {
                        Text(stringResource(R.string.feedback_char_count, uiState.charCount))
                    },
                    minLines = 4,
                    maxLines = 8
                )

                Spacer(modifier = Modifier.height(ElementSpacing))

                OutlinedTextField(
                    value = uiState.contactName,
                    onValueChange = onContactNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.feedback_contact_name_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(ElementSpacing))

                OutlinedTextField(
                    value = uiState.contactEmail,
                    onValueChange = onContactEmailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.EmailAddress },
                    label = { Text(stringResource(R.string.feedback_contact_email_hint)) },
                    singleLine = true,
                    isError = uiState.contactEmail.isNotEmpty() && !uiState.isEmailValid,
                    supportingText = if (uiState.contactEmail.isNotEmpty() && !uiState.isEmailValid) {
                        { Text(stringResource(R.string.feedback_contact_email_error)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(ElementSpacing))

                Button(
                    onClick = onSubmit,
                    enabled = uiState.isValid && !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(SmallLoadingIndicatorSize),
                            strokeWidth = SubtleSpacing,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.feedback_submit))
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(SectionSpacing)
            )
        }
    }
}
