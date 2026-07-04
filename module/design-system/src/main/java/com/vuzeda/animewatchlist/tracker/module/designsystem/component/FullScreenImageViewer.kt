package com.vuzeda.animewatchlist.tracker.module.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.vuzeda.animewatchlist.tracker.module.designsystem.R
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.AnimeWatchlistTrackerTheme
import com.vuzeda.animewatchlist.tracker.module.designsystem.theme.ElementSpacing

private val ViewerBackground = Color.Black
private val ViewerImagePlaceholder = Color(0xFF1A1A1A)
private val ViewerControlTint = Color.White

@Composable
fun FullScreenImageViewer(
    modifier: Modifier = Modifier,
    imageUrl: String,
    contentDescription: String?,
    imageModifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViewerBackground)
            .clickable(onClick = onDismiss)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = imageModifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            placeholder = ColorPainter(ViewerImagePlaceholder),
            error = ColorPainter(ViewerImagePlaceholder),
            fallback = ColorPainter(ViewerImagePlaceholder)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(ElementSpacing)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_close),
                tint = ViewerControlTint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FullScreenImageViewerPreview() {
    AnimeWatchlistTrackerTheme(dynamicColor = false) {
        FullScreenImageViewer(
            imageUrl = "https://example.com/attack-on-titan-cover.jpg",
            contentDescription = "Attack on Titan: Final Season Part 3",
            onDismiss = {}
        )
    }
}
