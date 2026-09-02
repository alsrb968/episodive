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
import kotlin.math.exp

/** 막대 치수 (원본 줄 436·471·584). */
private val WaveBarWidth = 3.dp
private val WaveBarMaxHeight = 16.dp
private val WaveBarGap = 2.dp
private val WaveBarCornerRadius = 1.5.dp

/** 소리가 최대일 때 막대가 닿는 높이 비율. */
private const val WaveMaxHeightFraction = 1f

/**
 * 소리가 없을 때의 높이 비율.
 *
 * 이 값이 곧 변화의 바닥이라, 높으면 큰 소리와 작은 소리의 차이가 눌려 보인다. 다만 트랙
 * 16dp 기준 0.2 아래로 내리면 높이가 막대 폭(3dp)과 비슷해져 캡슐이 아니라 점이 된다.
 */
private const val WaveRestHeightFraction = 0.3f

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
 * 막대 다섯은 각자 하나의 주파수 대역을 맡는다. 예전에는 하나의 크기에 고정 비율을 곱해
 * 모양을 만들었는데 그 모양은 소리와 무관한 UI 의 장식이었다. 지금은 모양 자체가 소리다.
 *
 * 막대에 자체 리듬은 없다. 예전에는 사인파를 합성해 늘 흔들리게 했는데, 그 움직임이 소리에
 * 따른 변화와 섞여 **무엇에 반응하는 것인지 읽히지 않았다.** 지금은 오직 [bandLevel] 만이
 * 높이를 정한다 — 소리가 없으면 다섯이 함께 한 줄로 잠잠하고, 소리가 커지면 각자의 대역에
 * 맞춰 따로 자란다.
 *
 * 막대 높이는 물리적 세기가 아니다. dB 창에서의 상대적 위치일 뿐이라, 두 막대의 높이가
 * 같다고 그 대역의 에너지가 같은 것은 아니다.
 *
 * @param bandLevel 막대 번호(0 부터, 낮은 주파수부터)를 받아 그 대역의 세기(0..1)를 그리기
 * 시점에 답하는 람다. 값을 State 로 받지 않고 람다로 받는 것은, 초당 수십 번 바뀌는 값을
 * 컴포지션에서 읽으면 그 빈도로 재구성이 일어나기 때문이다. 기본값은 소리를 모르는
 * 호출자(프리뷰·다른 화면)를 위한 [waveIdleBandLevel] 이다.
 */
@Composable
fun WaveAnimationIcon(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = MaterialTheme.colorScheme.primary,
    isAnimating: Boolean = true,
    bandLevel: (band: Int) -> Float = ::waveIdleBandLevel,
) {
    // 그리기 단계에서만 읽는 값이라 재구성이 아니라 다시 그리기만 일으킨다. 막대마다 독립된
    // 상태를 가져야 각자의 대역을 따로 좇을 수 있다.
    val levels = remember(barCount) { List(barCount) { mutableFloatStateOf(0f) } }

    // isAnimating, barCount 를 키로 둔다. LaunchedEffect(Unit) 이면 처음 들어온 값을 계속
    // 붙들어, 일시정지해도 막대가 그대로 움직인다. barCount 가 빠지면 levels 가 새로
    // remember 된 뒤에도 이펙트가 옛 리스트를 붙든다.
    LaunchedEffect(isAnimating, barCount) {
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
                val follow = 1f - exp(-elapsed / WaveFollowSeconds)

                for (index in 0 until barCount) {
                    // 멈췄으면 목표는 0 이다 — 잦아드는 모습이 곧 정지 표현이라 따로 둘 것이 없다.
                    val target = if (isAnimating) bandLevel(index).coerceIn(0f, 1f) else 0f
                    levels[index].floatValue += (target - levels[index].floatValue) * follow
                }
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
            Canvas(
                modifier = Modifier
                    .width(WaveBarWidth)
                    .height(WaveBarMaxHeight)
            ) {
                val fraction = WaveRestHeightFraction +
                        (WaveMaxHeightFraction - WaveRestHeightFraction) * levels[index].floatValue
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

private val WaveIdleBandLevels = floatArrayOf(0.2f, 0.46f, 1f, 0.46f, 0.2f)

/**
 * 소리를 모르는 호출자(프리뷰·다른 화면)를 위한 정적 모양. 예전 파도 아이콘의 코사인 곡선
 * 그대로다 — 단 **막대가 다섯일 때만** 그렇다. 표를 다시 계산하지 않고 남는 자리는 양끝 값
 * (0.2)으로 답하므로, 막대 수를 바꾸려면 이 표도 함께 손봐야 대칭이 유지된다.
 */
fun waveIdleBandLevel(band: Int): Float = WaveIdleBandLevels.getOrElse(band) { WaveIdleBandLevels.last() }

@ThemePreviews
@Composable
private fun WaveAnimationIconPreview() {
    EpisodiveTheme {
        WaveAnimationIcon(
            isAnimating = true
        )
    }
}
