package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Velocity

/**
 * fling 종료 시 잔여 속도로 인한 가장자리 stretch 바운스가 없는 [OverscrollEffect]를 반환한다.
 *
 * 콘텐츠가 짧은 리스트(홈 시트, 플레이어)는 대부분의 fling이 가장자리에 부딪히며 끝나는데,
 * 이때 잔여 속도가 stretch 효과로 흡수되면서 스크롤이 멈추는 순간 콘텐츠가 반대 방향으로
 * 튕기는 움찔거림이 발생한다. 손가락으로 당길 때의 stretch 피드백과 release 애니메이션은
 * 그대로 유지된다.
 */
@Composable
fun rememberOverscrollEffectWithoutFlingBounce(): OverscrollEffect? {
    val inner = rememberOverscrollEffect()
    return remember(inner) { inner?.withoutFlingBounce() }
}

/** fling 잔여 속도를 가장자리 stretch로 흡수하지 않는 래핑된 [OverscrollEffect]를 반환한다. */
@Stable
fun OverscrollEffect.withoutFlingBounce(): OverscrollEffect =
    WithoutFlingBounceOverscrollEffect(this)

private class WithoutFlingBounceOverscrollEffect(
    private val inner: OverscrollEffect,
) : OverscrollEffect {

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset = inner.applyToScroll(delta, source, performScroll)

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        inner.applyToFling(velocity) { available ->
            performFling(available)
            // 잔여 속도를 모두 소비한 것으로 보고해, 원본 효과가 남은 속도를
            // 가장자리 stretch(바운스)로 흡수하지 못하게 한다. 드래그로 이미 쌓인
            // stretch 의 release 는 원본 applyToFling 흐름이 그대로 처리한다.
            available
        }
    }

    override val isInProgress: Boolean
        get() = inner.isInProgress

    override val node: DelegatableNode
        get() = inner.node
}
