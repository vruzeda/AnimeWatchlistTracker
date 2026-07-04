package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.painter.ColorPainter

@Composable
@ReadOnlyComposable
fun coverPlaceholderPainter(): ColorPainter = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
