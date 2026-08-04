package io.jacob.episodive.core.designsystem.component

import android.graphics.drawable.BitmapDrawable
import androidx.annotation.Px
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews
import kotlin.math.abs

@Composable
fun StateImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    @Px size: Int = 300,
    contentDescription: String?,
    // 커버를 못 그릴 때 대신 얹을 머리글자의 출처. 호출부는 거의 항상 contentDescription 에
    // 제목을 넘기므로 그것을 기본값으로 재사용한다. 제목이 아닌 설명을 넘기는 곳만 직접 지정하면 된다.
    title: String? = contentDescription,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderBrush: Brush = thumbnailPlaceholderDefaultBrush(imageUrl),
    // 색 추출 규칙(필터·휘도 클램프)은 전부 EpisodiveDominantColor 가 갖는다.
    // 화면마다 보정값을 따로 주면 같은 커버에서 화면마다 다른 색이 나오므로 여기서 열지 않는다.
    onDominantColorExtracted: ((Color) -> Unit)? = null,
    dominantRegion: DominantRegion = DominantRegion.Full,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(placeholderBrush))
        return
    }

    // 커버가 아예 없는 항목이 드물지 않다(에피소드 절반 이상이 자체 이미지가 없다). 빈 URL 을
    // 그대로 Coil 에 넘기면 실패할 요청을 한 번 왕복시키고 나서야 같은 자리에 같은 면을 그린다.
    if (imageUrl.isBlank()) {
        Box(modifier = modifier) {
            CoverPlaceholder(placeholderBrush, title, contentDescription)
        }
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
            // 실패를 로딩과 같은 면으로 그린다. 관측된 실패는 전부 서버 쪽 결함(만료 인증서,
            // 핫링크 403, 없는 파일)이라 사용자가 취할 행동이 없는데, 목록에 경고 아이콘이
            // 흩뿌려지면 앱이 고장난 것처럼 읽힌다. 머리글자를 얹은 정지된 면은 "커버가 없는
            // 팟캐스트"로 자연스럽게 읽히고, 그게 실제로 일어난 일에 더 가깝다.
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Error,
                -> {
                // 여기에 shimmer 를 넣지 않는다. 스켈레톤 카드가 사라지고 실제 카드가 뜬 직후
                // 이미지는 아직 로딩 중인 경우가 많아 "끝난 줄 알았는데 또 반짝"이 되고,
                // 스크롤 중에는 늘 몇 장이 로딩 중이라 목록이 상시 명멸한다. 정지된 면으로 둔다.
                // contentDescription 은 아래 Image 가 들고 있으므로 여기서는 넘기지 않는다.
                CoverPlaceholder(placeholderBrush, title, contentDescription = null)
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

/**
 * 커버를 그릴 수 없을 때 자리를 지키는 면 — 로딩 중, URL 없음, 로드 실패 셋 모두 여기로 온다.
 * 제목 머리글자를 얹어 "아직 안 온 빈 칸"이 아니라 "원래 이렇게 생긴 커버"로 읽히게 한다.
 */
@Composable
private fun CoverPlaceholder(
    brush: Brush,
    title: String?,
    contentDescription: String?,
) {
    BoxWithConstraints(
        modifier = Modifier
            .background(brush)
            .fillMaxSize()
            // 이 분기에는 Image 가 없어 설명을 붙일 노드가 사라진다. 그대로 두면 TalkBack 이
            // 제목 대신 머리글자 한 글자만 읽는다.
            .semantics { contentDescription?.let { this.contentDescription = it } },
        contentAlignment = Alignment.Center,
    ) {
        // 부모가 무한 제약을 주면 side 가 Dp.Infinity 가 되어 폰트 크기가 발산한다.
        val side = minOf(maxWidth, maxHeight).coerceAtMost(CoverInitialMaxSide)
        val initial = title?.coverInitial()

        // 작은 썸네일에서는 글자가 뭉개져 노이즈만 된다. 그럴 땐 면만 남긴다.
        if (initial != null && side >= CoverInitialMinSide) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = CoverInitialAlpha),
                fontSize = with(LocalDensity.current) { (side * CoverInitialSideRatio).toSp() },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                // 머리글자는 장식이다. 위에서 붙인 설명과 겹쳐 읽히지 않게 가린다.
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}

/** 제목의 첫 글자. 이모지나 문장부호로 시작하는 제목은 글자를 포기하고 면만 남긴다. */
private fun String.coverInitial(): String? =
    trim().firstOrNull()?.takeIf(Char::isLetterOrDigit)?.uppercase()

private val CoverInitialMinSide = 40.dp
private val CoverInitialMaxSide = 240.dp
private const val CoverInitialSideRatio = 0.38f
private const val CoverInitialAlpha = 0.55f

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