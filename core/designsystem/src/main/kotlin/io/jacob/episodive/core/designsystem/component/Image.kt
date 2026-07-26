package io.jacob.episodive.core.designsystem.component

import android.graphics.drawable.BitmapDrawable
import androidx.annotation.Px
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import timber.log.Timber
import kotlin.math.abs

@Composable
fun StateImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    @Px size: Int = 300,
    contentDescription: String?,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderBrush: Brush = thumbnailPlaceholderDefaultBrush(imageUrl),
    fallbackIcon: ImageVector = EpisodiveIcons.Error,
    // 색 추출 규칙(필터·휘도 클램프)은 전부 EpisodiveDominantColor 가 갖는다.
    // 화면마다 보정값을 따로 주면 같은 커버에서 화면마다 다른 색이 나오므로 여기서 열지 않는다.
    onDominantColorExtracted: ((Color) -> Unit)? = null,
    dominantRegion: DominantRegion = DominantRegion.Full,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(placeholderBrush))
        return
    }

    var imagePainterState by remember {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    val context = LocalContext.current
    val imageRequest = remember(imageUrl, size, onDominantColorExtracted != null) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(size)
            .apply {
                if (onDominantColorExtracted != null) {
                    allowHardware(false)
                    listener(
                        onSuccess = { _, result ->
                            val drawable = result.drawable
                            val bitmap = (drawable as? BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                EpisodiveDominantColor.extract(bitmap, dominantRegion)
                                    ?.let(onDominantColorExtracted)
                            }
                        }
                    )
                }
            }
            .build()
    }

    val imageLoader = rememberAsyncImagePainter(
        model = imageRequest,
        contentScale = contentScale,
        onState = { state -> imagePainterState = state }
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (imagePainterState) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading,
                -> {
            }

            is AsyncImagePainter.State.Error,
                -> {
                Timber.w("Image($imageUrl) load error: ${(imagePainterState as? AsyncImagePainter.State.Error)?.result?.throwable.toString()}")
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .fillMaxSize()
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp),
                        imageVector = fallbackIcon,
                        contentDescription = null,
                    )
                }
            }

            is AsyncImagePainter.State.Success -> {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onBackground)
                        .fillMaxSize()
                )
            }
        }

        Image(
            painter = imageLoader,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

/** 팟캐스트별로 고정된 시드 색을 순환 배정해 커버 아트 플레이스홀더에 변주를 준다. */
private val CoverPlaceholderSeeds = listOf(
    Color(0xFFC6472F),
    Color(0xFF2E7D6B),
    Color(0xFF7A3B8F),
    Color(0xFF2B5C8A),
    Color(0xFFC99A2E),
    Color(0xFF3A7D5B),
)
private val CoverPlaceholderEnd = Color(0xFF1A1413)

internal fun thumbnailPlaceholderDefaultBrush(imageUrl: String? = null): Brush {
    val seed = imageUrl
        ?.let { CoverPlaceholderSeeds[abs(it.hashCode()) % CoverPlaceholderSeeds.size] }
        ?: CoverPlaceholderSeeds.first()

    return Brush.linearGradient(
        colorStops = arrayOf(0f to seed, 0.18f to seed, 1f to CoverPlaceholderEnd),
    )
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