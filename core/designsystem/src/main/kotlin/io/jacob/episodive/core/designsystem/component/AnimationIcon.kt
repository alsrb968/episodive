package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow

/** 막대 치수 (원본 줄 436·471·584). */
private val WaveBarWidth = 3.dp
private val WaveBarMaxHeight = 16.dp
private val WaveBarGap = 2.dp
private val WaveBarCornerRadius = 1.5.dp

/** 소리가 최대일 때 가운데 막대가 닿는 높이 비율. */
private const val WaveMaxHeightFraction = 1f

/**
 * 소리가 없을 때의 높이 비율.
 *
 * 이 값이 곧 변화의 바닥이라, 높으면 큰 소리와 작은 소리의 차이가 눌려 보인다. 다만 트랙
 * 16dp 기준 0.2 아래로 내리면 높이가 막대 폭(3dp)과 비슷해져 캡슐이 아니라 점이 된다.
 */
private const val WaveRestHeightFraction = 0.3f

/** 양 끝 막대가 소리에 반응하는 정도. 가운데는 1, 끝은 이 값이다. */
private const val WaveEdgeResponse = 0.2f

/**
 * 가운데로 몰아주는 정도. 1 이면 코사인 그대로이고, 키울수록 가운데만 크게 솟고 옆은 빨리
 * 눕는다. 다섯 막대 기준 1.6 이면 대략 0.2 / 0.46 / 1.0 / 0.46 / 0.2 이 된다.
 */
private const val WaveCenterBias = 1.6f

/**
 * 크기 변화를 따라가는 데 걸리는 시간(초).
 *
 * 소리 크기는 초당 서른 번쯤 새 값이 오는데, 그대로 그리면 60fps 화면에서 두 프레임마다
 * 높이가 뚝 끊겨 계단처럼 보인다. 매 프레임 목표를 향해 이 시간 상수로 좇는다.
 */
private const val WaveFollowSeconds = 0.045f

/** 한 프레임에 반영할 최대 시간. 화면이 멈췄다 돌아올 때 높이가 튀지 않게 막는다. */
private const val WaveMaxFrameSeconds = 0.1f

private const val NanosPerSecond = 1_000_000_000f

/**
 * 재생 중인 소리의 크기를 막대 다섯 개로 보여준다.
 *
 * 막대에 자체 리듬은 없다. 예전에는 사인파를 합성해 늘 흔들리게 했는데, 그 움직임이 소리에
 * 따른 변화와 섞여 **무엇에 반응하는 것인지 읽히지 않았다.** 지금은 오직 [amplitude] 만이
 * 높이를 정한다 — 소리가 없으면 가운데 한 줄로 잠잠하고, 소리가 커지면 위아래로 자란다.
 *
 * 가운데일수록 크게, 바깥일수록 덜 움직인다([WaveEdgeResponse]). 다섯 막대가 똑같이
 * 오르내리면 이퀄라이저가 아니라 그냥 커졌다 작아지는 덩어리로 보인다.
 *
 * @param amplitude 지금 나고 있는 소리의 크기(0..1)를 그리기 시점에 답하는 람다. 값을 State 로
 * 받지 않고 람다로 받는 것은, 초당 수십 번 바뀌는 값을 컴포지션에서 읽으면 그 빈도로 재구성이
 * 일어나기 때문이다. 기본값은 소리를 모르는 호출자(프리뷰·다른 화면)를 위한 "늘 최대" 다.
 */
@Composable
fun WaveAnimationIcon(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = MaterialTheme.colorScheme.primary,
    isAnimating: Boolean = true,
    amplitude: () -> Float = { 1f },
) {
    // 막대별 반응 정도. 가운데 1 에서 양 끝 WaveEdgeResponse 까지 코사인으로 눕힌다.
    val responses = remember(barCount) { barResponses(barCount) }

    // 그리기 단계에서만 읽는 값이라 재구성이 아니라 다시 그리기만 일으킨다.
    val level = remember { mutableFloatStateOf(0f) }

    // isAnimating 을 키로 둔다. LaunchedEffect(Unit) 이면 처음 들어온 값을 계속 붙들어,
    // 일시정지해도 막대가 그대로 움직인다.
    LaunchedEffect(isAnimating) {
        var lastFrameNanos = 0L
        while (true) {
            // withFrameNanos 가 아니라 이 쪽이다. 끝나지 않는 애니메이션이라는 사실을
            // 프레임워크에 알려, Compose 테스트가 "화면이 멎기" 를 영영 기다리지 않는다.
            // (withFrameNanos 로 두었더니 ClipScreenTest 다섯 개가 그 자리에서 멈췄다.)
            withInfiniteAnimationFrameNanos { frameNanos ->
                val elapsed = if (lastFrameNanos == 0L) {
                    0f
                } else {
                    ((frameNanos - lastFrameNanos) / NanosPerSecond).coerceAtMost(WaveMaxFrameSeconds)
                }
                lastFrameNanos = frameNanos

                // 멈췄으면 목표는 0 이다 — 잦아드는 모습이 곧 정지 표현이라 따로 둘 것이 없다.
                val target = if (isAnimating) amplitude().coerceIn(0f, 1f) else 0f
                val follow = 1f - exp(-elapsed / WaveFollowSeconds)
                level.floatValue += (target - level.floatValue) * follow
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WaveBarGap),
        // 세로 가운데에서 위아래로 함께 자란다. 바닥에 붙여 두면 pill 안에서 무게가 아래로
        // 쏠려, 가운데 정렬된 옆 시간 텍스트와 어긋나 보인다.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(barCount) { index ->
            val response = responses[index]

            Canvas(
                modifier = Modifier
                    .width(WaveBarWidth)
                    .height(WaveBarMaxHeight)
            ) {
                val fraction = WaveRestHeightFraction +
                        (WaveMaxHeightFraction - WaveRestHeightFraction) * level.floatValue * response
                val barHeight = size.height * fraction

                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, (size.height - barHeight) / 2f),
                    size = Size(size.width, barHeight),
                    cornerRadius = CornerRadius(WaveBarCornerRadius.toPx()),
                )
            }
        }
    }
}

/**
 * 가운데 1, 양 끝 [WaveEdgeResponse] 로 눕는 막대별 반응 정도.
 *
 * 선형으로 줄이면 꺾인 삼각형이 되어 가운데만 튀어나온 것처럼 보인다. 코사인으로 눕히면
 * 이웃한 막대끼리 높이 차가 고르게 벌어져 하나의 모양으로 읽힌다.
 */
private fun barResponses(barCount: Int): List<Float> {
    val center = (barCount - 1) / 2f
    return List(barCount) { index ->
        // 막대가 하나뿐이면 그 하나가 곧 가운데다.
        if (center == 0f) return@List 1f

        val distance = abs(index - center) / center
        val shape = (0.5f * (1f + cos(PI.toFloat() * distance))).pow(WaveCenterBias)
        WaveEdgeResponse + (1f - WaveEdgeResponse) * shape
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
