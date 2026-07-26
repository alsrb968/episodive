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
import androidx.compose.ui.graphics.lerp
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
            // 플레이스홀더에서 실제 이미지로 툭 끊기지 않게 넘긴다.
            .crossfade(ImageCrossfadeMs)
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
                // 여기에 shimmer 를 넣지 않는다. 스켈레톤 카드가 사라지고 실제 카드가 뜬 직후
                // 이미지는 아직 로딩 중인 경우가 많아 "끝난 줄 알았는데 또 반짝"이 되고,
                // 스크롤 중에는 늘 몇 장이 로딩 중이라 목록이 상시 명멸한다. 정지된 면으로 둔다.
                Box(
                    modifier = Modifier
                        .background(placeholderBrush)
                        .fillMaxSize()
                )
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

private const val ImageCrossfadeMs = 200

/** 팟캐스트별로 고정된 시드 색을 순환 배정해 커버 아트 플레이스홀더에 변주를 준다. */
private val CoverPlaceholderSeeds = listOf(
    Color(0xFFC6472F),
    Color(0xFF2E7D6B),
    Color(0xFF7A3B8F),
    Color(0xFF2B5C8A),
    Color(0xFFC99A2E),
    Color(0xFF3A7D5B),
)

/**
 * 시드를 표면색 쪽으로 얼마나 끌어올지.
 *
 * 시드를 날것 그대로 쓰면 라이트 테마에서 채도 높은 어두운 사각형이 되고, 무엇보다 스켈레톤
 * 커버 블록(surfaceContainerHigh 단색)과 색이 달라 스켈레톤이 걷힌 자리에서 커버만 색이
 * 확 바뀐다. 강하게 섞어 두면 팟캐스트별 미세한 변주는 남으면서 전환이 조용해진다.
 */
private const val CoverPlaceholderSeedBlend = 0.8f

@Composable
internal fun thumbnailPlaceholderDefaultBrush(imageUrl: String? = null): Brush {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val end = MaterialTheme.colorScheme.surfaceContainer

    val seed = imageUrl
        ?.let { CoverPlaceholderSeeds[abs(it.hashCode()) % CoverPlaceholderSeeds.size] }
        ?: CoverPlaceholderSeeds.first()
    val blended = lerp(seed, base, CoverPlaceholderSeedBlend)

    return Brush.linearGradient(
        colorStops = arrayOf(0f to blended, 0.18f to blended, 1f to end),
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