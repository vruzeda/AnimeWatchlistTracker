package com.vuzeda.animewatchlist.tracker.module.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val statusWatching: Color,
    val statusCompleted: Color,
    val statusPlanToWatch: Color,
    val statusOnHold: Color,
    val statusDropped: Color,
    val ratingActive: Color,
    val ratingInactive: Color
)

val LightExtendedColors = ExtendedColors(
    statusWatching = Color(0xFF2E7D32),
    statusCompleted = Color(0xFF1565C0),
    statusPlanToWatch = Color(0xFFE65100),
    statusOnHold = Color(0xFF616161),
    statusDropped = Color(0xFFC62828),
    ratingActive = Color(0xFFF9A825),
    ratingInactive = Color(0xFF9E9E9E)
)

val DarkExtendedColors = ExtendedColors(
    statusWatching = Color(0xFF81C784),
    statusCompleted = Color(0xFF64B5F6),
    statusPlanToWatch = Color(0xFFFFB74D),
    statusOnHold = Color(0xFFBDBDBD),
    statusDropped = Color(0xFFE57373),
    ratingActive = Color(0xFFFFD700),
    ratingInactive = Color(0xFF757575)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
