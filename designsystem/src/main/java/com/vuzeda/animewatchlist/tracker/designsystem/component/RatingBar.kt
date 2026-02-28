package com.vuzeda.animewatchlist.tracker.designsystem.component

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vuzeda.animewatchlist.tracker.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.designsystem.theme.RatingEmpty
import com.vuzeda.animewatchlist.tracker.designsystem.theme.RatingGold

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Int,
    maxRating: Int = 10,
    starSize: Dp = 24.dp,
    isInteractive: Boolean = false,
    onRatingChanged: (Int) -> Unit = {}
) {
    val starSizePx = with(LocalDensity.current) { starSize.toPx() }

    val gestureModifier = if (isInteractive) {
        var dragStartX by remember { mutableFloatStateOf(0f) }
        Modifier
            .pointerInput(maxRating, starSizePx) {
                detectTapGestures { offset ->
                    val tappedRating = ((offset.x / starSizePx).toInt() + 1).coerceIn(1, maxRating)
                    onRatingChanged(tappedRating)
                }
            }
            .pointerInput(maxRating, starSizePx) {
                detectHorizontalDragGestures(
                    onDragStart = { offset -> dragStartX = offset.x },
                    onHorizontalDrag = { _, dragAmount ->
                        dragStartX += dragAmount
                        val draggedRating = ((dragStartX / starSizePx).toInt() + 1).coerceIn(1, maxRating)
                        onRatingChanged(draggedRating)
                    }
                )
            }
    } else {
        Modifier
    }

    Row(modifier = modifier.then(gestureModifier)) {
        (1..maxRating).forEach { index ->
            val isFilled = index <= rating
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $index of $maxRating",
                modifier = Modifier.size(starSize),
                tint = if (isFilled) RatingGold else RatingEmpty
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        RatingBar(
            modifier = Modifier.padding(16.dp),
            rating = 7,
            maxRating = 10
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarEmptyPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        RatingBar(
            modifier = Modifier.padding(16.dp),
            rating = 0,
            maxRating = 10
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingBarInteractivePreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        RatingBar(
            modifier = Modifier.padding(16.dp),
            rating = 5,
            maxRating = 10,
            isInteractive = true
        )
    }
}
