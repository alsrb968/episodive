package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun StateImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderBrush: Brush = thumbnailPlaceholderDefaultBrush(),
    fallbackIcon: ImageVector = EpisodiveIcons.Place,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(placeholderBrush))
        return
    }

    var imagePainterState by remember {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    val imageLoader = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentScale = contentScale,
        onState = { state -> imagePainterState = state }
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (imagePainterState) {
//            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Error -> {
                Image(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .background(placeholderBrush)
                        .fillMaxSize()
                )
            }
        }

        Image(
            painter = imageLoader,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}

@Composable
internal fun thumbnailPlaceholderDefaultBrush(
    color: Color = MaterialTheme.colorScheme.primaryContainer
): Brush {
    return SolidColor(color)
}

@ThemePreviews
@Composable
private fun StateImagePreview() {
    EpisodiveTheme {
        StateImage(
            imageUrl = "https://www.example.com/image.jpg",
            contentDescription = "Example Image",
            modifier = Modifier
                .size(16.dp)
        )
    }
}