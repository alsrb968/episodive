package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

/** 막대 치수 (원본 줄 436·471·584). */
private val WaveBarWidth = 3.dp
private val WaveBarMaxHeight = 16.dp
private val WaveBarGap = 2.dp
private val WaveBarCornerRadius = 1.5.dp

/**
 * 막대별 (최소 높이 비율, 최대 높이 비율, 왕복 주기 ms).
 *
 * 주기를 서로 어긋난 값으로 두어 막대가 한 덩어리로 같이 움직이지 않게 한다. 이전에는
 * 매 컴포지션마다 `random()` 으로 주기를 뽑아 같은 화면을 다시 그릴 때마다 리듬이 바뀌었다.
 */
private val WaveBarProfiles = listOf(
    Triple(0.30f, 0.72f, 520),
    Triple(0.42f, 1.00f, 400),
    Triple(0.24f, 0.86f, 620),
    Triple(0.38f, 0.94f, 460),
    Triple(0.32f, 0.66f, 560),
)

/**
 * 사인파에 가까운 ease-in-out. 기존 [androidx.compose.animation.core.LinearEasing] 은 위아래
 * 끝점에서 방향이 각지게 꺾여 이퀄라이저가 아니라 깜빡이는 막대처럼 보였다.
 */
private val WaveBarEasing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

/** 막대 사이 시작 시점 어긋남 — 왼쪽에서 오른쪽으로 흐르는 느낌을 준다. */
private const val WaveBarStaggerMs = 90

/** 멈춰 있을 때의 높이 비율. 너무 낮추면 막대가 아니라 점처럼 보인다. */
private const val WaveRestHeightFraction = 0.38f

/** 재생/정지 전환에 걸리는 시간 — 멈출 때 높이가 뚝 끊기지 않게 한다. */
private const val WaveSettleDurationMs = 260

@Composable
fun WaveAnimationIcon(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = MaterialTheme.colorScheme.primary,
    isAnimating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    // 정지 상태로 가는 정도(1 = 재생 중, 0 = 멈춤). 높이 자체가 아니라 진폭을 줄여서,
    // 재생 중에는 감쇠 없이 온전한 폭으로 움직이고 멈출 때만 쉬는 높이로 부드럽게 수렴한다.
    val activeAmplitude by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(durationMillis = WaveSettleDurationMs, easing = WaveBarEasing),
        label = "waveAmplitude",
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WaveBarGap),
        // 원본은 align-items:flex-end (원본 줄 436·471·584). 중앙 정렬이면 막대가
        // 위아래로 자라 이퀄라이저가 아니라 떠 있는 점선처럼 보인다.
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val (minFraction, maxFraction, durationMillis) =
                WaveBarProfiles[index % WaveBarProfiles.size]

            // by 로 풀지 않고 State 를 그대로 들고 있다가 그리기 단계에서 읽는다. 매 프레임
            // 높이를 바꿔도 재구성·재측정 없이 다시 그리기만 한다.
            val animatedFraction = infiniteTransition.animateFloat(
                initialValue = minFraction,
                targetValue = maxFraction,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = durationMillis,
                        easing = WaveBarEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * WaveBarStaggerMs)
                ),
                label = "bar$index"
            )

            Canvas(
                modifier = Modifier
                    .width(WaveBarWidth)
                    .height(WaveBarMaxHeight)
            ) {
                val fraction = WaveRestHeightFraction +
                        (animatedFraction.value - WaveRestHeightFraction) * activeAmplitude
                val barHeight = size.height * fraction

                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, size.height - barHeight),
                    size = Size(size.width, barHeight),
                    cornerRadius = CornerRadius(WaveBarCornerRadius.toPx()),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun WaveAnimationIconPreview() {
    EpisodiveTheme {
        WaveAnimationIcon(
            isAnimating = true
        )
    }
}
