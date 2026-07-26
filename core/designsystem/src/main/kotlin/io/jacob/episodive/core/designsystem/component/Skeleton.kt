package io.jacob.episodive.core.designsystem.component

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.R
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 스켈레톤 서브트리 위로 빛 한 줄기를 쓸어 보낸다.
 *
 * 블록마다 걸지 말고 스켈레톤 **루트에 한 번만** 건다. 블록마다 걸면 저마다 자기 좌표계에서
 * 0→1 을 돌기 때문에 118dp 카드와 340dp 행이 서로 다른 속도로 명멸한다 — 로딩 표시가 아니라
 * 고장 난 표시등으로 보인다. 여기서는 오프스크린 레이어를 한 장 만들고 그 위를
 * [BlendMode.SrcAtop] 으로 한 번만 칠한다. SrcAtop 은 이미 그려진 픽셀 위에만 얹히므로
 * 블록 자리에만 빛이 들고 빈 자리는 그대로 통과한다. 그리기가 컨테이너 좌표계에서 한 번
 * 일어나므로 블록별 좌표를 모을 필요가 없다.
 *
 * 제약 두 가지 — 지키지 않으면 조용히 망가진다:
 * - **이 Modifier 를 건 노드에 배경을 칠하지 말 것.** 레이어 전체가 "그려진 픽셀"이 되어
 *   화면이 통째로 균일하게 쓸린다. 배경이 필요하면 바깥 노드에서 칠한다.
 * - **스크롤하는 리스트 전체에 걸지 말 것.** 스크롤할 때마다 레이어가 무효화된다. 페이징
 *   푸터처럼 스크롤과 함께 움직이는 작은 서브트리에는 따로 건다.
 *
 * 정식 진입점은 [SkeletonContainer] 다. 이 Modifier 는 컨테이너를 쓸 수 없는 예외용이다.
 */
@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.shimmerSweep(
    enabled: Boolean = true,
    highlight: Color = Color.Unspecified,
    durationMillis: Int = SkeletonDefaults.SWEEP_DURATION_MS,
    bandWidthFraction: Float = SkeletonDefaults.BAND_WIDTH_FRACTION,
    angleDegrees: Float = SkeletonDefaults.ANGLE_DEGREES,
) = composed {
    val sweepColor = highlight.takeOrElse { SkeletonDefaults.highlightColor() }

    // 정적 프리뷰는 한 프레임만 그려 무한 애니메이션이 0 에 멈춘다(= 빛이 화면 밖에 있다).
    // 보기 좋은 위상에 고정해 프리뷰에서도 빛이 보이게 한다.
    val phase: State<Float> = if (LocalInspectionMode.current) {
        remember { mutableFloatStateOf(SkeletonDefaults.INSPECTION_PHASE) }
    } else {
        rememberInfiniteTransition(label = "skeletonShimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
            ),
            label = "skeletonShimmerPhase",
        )
    }

    if (!enabled) return@composed this

    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val radians = angleDegrees * (PI.toFloat() / 180f)
            val dirX = cos(radians)
            val dirY = sin(radians)

            // 콘텐츠를 진행 방향으로 투영한 길이. 빛의 띠는 이 축 위를 지나간다.
            val span = abs(size.width * dirX) + abs(size.height * dirY)
            val band = (span * bandWidthFraction).coerceAtLeast(1f)
            val travel = span + band * 2f

            onDrawWithContent {
                drawContent()

                // 애니메이션 값은 그리기 단계에서만 읽는다. 컴포저블 본문에서 읽으면 매 프레임
                // 서브트리 전체가 리컴포즈된다.
                val head = -band + phase.value * travel

                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.5f to sweepColor,
                            1f to Color.Transparent,
                        ),
                        start = Offset(dirX * head, dirY * head),
                        end = Offset(dirX * (head + band), dirY * (head + band)),
                    ),
                    blendMode = BlendMode.SrcAtop,
                )
            }
        }
}

/**
 * 스켈레톤의 루트. 빛·등장 페이드·접근성을 한 번에 처리한다.
 *
 * 화면 작성자가 잊을 수 없도록 세 가지를 여기에 묶어 뒀다. 특히 접근성 — 회색 블록들은
 * TalkBack 에 아무것도 아니라서, 이 처리가 없으면 로딩 화면이 "빈 화면"으로 읽힌다.
 *
 * 배경은 이 컨테이너 **바깥**에서 칠해야 한다([shimmerSweep] 의 제약).
 */
@Composable
fun SkeletonContainer(
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.core_designsystem_loading),
    appearDelayMillis: Int = SkeletonDefaults.APPEAR_DELAY_MS,
    shimmerEnabled: Boolean = SkeletonDefaults.shimmerEnabled(),
    content: @Composable BoxScope.() -> Unit,
) {
    val isInspection = LocalInspectionMode.current

    // 로딩이 눈 깜짝할 사이에 끝나면 스켈레톤이 번쩍이고 사라져 스피너보다 산만하다.
    // 지연 뒤에 페이드로 들어와, 짧은 로딩에서는 아예 보이지 않게 한다.
    val appear = remember { Animatable(if (isInspection) 1f else 0f) }

    LaunchedEffect(appearDelayMillis) {
        if (isInspection) return@LaunchedEffect
        delay(appearDelayMillis.toLong())
        appear.animateTo(1f, tween(SkeletonDefaults.APPEAR_DURATION_MS))
    }

    Box(
        modifier = modifier
            .graphicsLayer { alpha = appear.value }
            .shimmerSweep(enabled = shimmerEnabled)
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                liveRegion = LiveRegionMode.Polite
            }
            .testTag(SkeletonDefaults.TEST_TAG),
        content = content,
    )
}

/** 콘텐츠 한 덩이가 들어갈 자리. 크기는 [modifier] 로 정한다. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = SkeletonDefaults.BlockShape,
    color: Color = SkeletonDefaults.baseColor(),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
    )
}

/**
 * 텍스트 한 줄이 들어갈 자리.
 *
 * 자리는 [style] 의 줄 높이를 그대로 차지하되 칠하는 높이는 그보다 얇다. 줄 높이를 꽉 채워
 * 칠하면 글자보다 뚱뚱해 보이고 줄 사이가 붙어 버린다 — 잉크가 차지하는 몫만 칠해야 실제
 * 텍스트와 같은 리듬이 나온다.
 */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    widthFraction: Float = 1f,
) {
    val density = LocalDensity.current
    val lineHeight = remember(style, density) {
        with(density) {
            when {
                style.lineHeight.isSp -> style.lineHeight.toDp()
                // em 단위이거나 지정되지 않은 스타일. 글자 크기로 어림한다.
                style.fontSize.isSp -> style.fontSize.toDp() * SkeletonDefaults.FALLBACK_LINE_RATIO
                else -> SkeletonDefaults.FallbackLineHeight
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(lineHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(lineHeight * SkeletonDefaults.LINE_INK_RATIO),
            shape = SkeletonDefaults.LineShape,
        )
    }
}

/** 커버 아트가 들어갈 자리. 반경은 [EpisodiveShapes.coverForSize] 사다리를 그대로 탄다. */
@Composable
fun SkeletonCover(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    SkeletonBox(
        modifier = modifier.size(size),
        shape = EpisodiveShapes.coverForSize(size.value.roundToInt()),
    )
}

object SkeletonDefaults {
    /** 빛의 띠 폭 — 진행 방향으로 투영한 콘텐츠 길이 대비 비율 */
    const val BAND_WIDTH_FRACTION = 0.6f

    /** 띠가 지나가는 각도. 완전한 수평보다 살짝 기울어야 빛처럼 읽힌다. */
    const val ANGLE_DEGREES = 18f

    const val HIGHLIGHT_ALPHA = 0.07f

    /** 줄 높이 대비 실제로 칠하는 높이 */
    const val LINE_INK_RATIO = 0.72f

    /** 줄 높이를 알 수 없을 때 글자 크기에 곱하는 비율 */
    const val FALLBACK_LINE_RATIO = 1.4f

    /** 정적 프리뷰에서 빛을 고정할 위상 */
    const val INSPECTION_PHASE = 0.35f

    const val SWEEP_DURATION_MS = 1400
    const val APPEAR_DELAY_MS = 120
    const val APPEAR_DURATION_MS = 220

    const val TEST_TAG = "skeleton"

    val BlockShape = RoundedCornerShape(8.dp)
    val LineShape = RoundedCornerShape(4.dp)
    val FallbackLineHeight = 16.dp

    @Composable
    fun baseColor(): Color = MaterialTheme.colorScheme.surfaceContainerHigh

    /**
     * 하이라이트를 흰색으로 고정하면 라이트 테마에서 보이지 않는다. onSurface 를 쓰면
     * 다크에서는 밝은 빛, 라이트에서는 어두운 빛이 되어 양쪽 다 대비가 생긴다.
     */
    @Composable
    fun highlightColor(): Color =
        MaterialTheme.colorScheme.onSurface.copy(alpha = HIGHLIGHT_ALPHA)

    /**
     * 시스템 애니메이션 배율이 0 이면 빛을 끈다.
     *
     * 동작 저감 설정을 켠 사용자에게 끊임없이 흐르는 빛은 그 자체로 방해다. 덤으로 애니메이션을
     * 끈 테스트 환경에서 무한 애니메이션이 대기를 붙잡는 문제도 함께 사라진다.
     */
    @Composable
    fun shimmerEnabled(): Boolean {
        if (LocalInspectionMode.current) return true

        val context = LocalContext.current
        return remember(context) {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) > 0f
        }
    }
}

@ThemePreviews
@Composable
private fun SkeletonPreview() {
    EpisodiveTheme {
        // 배경은 컨테이너 바깥에서 칠한다 — 안에서 칠하면 레이어 전체가 쓸린다.
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            SkeletonContainer {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    SkeletonCover(118.dp)
                    SkeletonLine(
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        widthFraction = 0.85f,
                    )
                    SkeletonLine(
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        widthFraction = 0.55f,
                    )
                }
            }
        }
    }
}
