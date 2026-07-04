package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class OverscrollTest {

    private class FakeOverscrollEffect : OverscrollEffect {
        var scrollDelta: Offset? = null
        var scrollSource: NestedScrollSource? = null
        var flingVelocity: Velocity? = null
        var leftoverForBounce: Velocity? = null

        override fun applyToScroll(
            delta: Offset,
            source: NestedScrollSource,
            performScroll: (Offset) -> Offset,
        ): Offset {
            scrollDelta = delta
            scrollSource = source
            return performScroll(delta)
        }

        override suspend fun applyToFling(
            velocity: Velocity,
            performFling: suspend (Velocity) -> Velocity,
        ) {
            flingVelocity = velocity
            val consumed = performFling(velocity)
            // 실제 AndroidOverscrollEffect 는 이 잔여 속도를 가장자리 stretch(바운스)로 흡수한다.
            leftoverForBounce = velocity - consumed
        }

        override val isInProgress: Boolean = false
        override val node: DelegatableNode = object : Modifier.Node() {}
    }

    @Test
    fun `Given wrapped effect, When fling leaves residual velocity, Then no leftover is reported for bounce`() =
        runTest {
            val inner = FakeOverscrollEffect()
            val wrapped = inner.withoutFlingBounce()
            val velocity = Velocity(0f, 3000f)

            wrapped.applyToFling(velocity) { available ->
                // 리스트가 가장자리에 걸려 속도를 전혀 소비하지 못한 상황
                available - available
            }

            assertEquals(velocity, inner.flingVelocity)
            assertEquals(Velocity.Zero, inner.leftoverForBounce)
        }

    @Test
    fun `Given wrapped effect, When fling occurs, Then list fling is performed with forwarded velocity`() =
        runTest {
            val inner = FakeOverscrollEffect()
            val wrapped = inner.withoutFlingBounce()
            var performedWith: Velocity? = null

            wrapped.applyToFling(Velocity(0f, -1500f)) { available ->
                performedWith = available
                available
            }

            assertEquals(Velocity(0f, -1500f), performedWith)
        }

    @Test
    fun `Given wrapped effect, When scrolling, Then scroll is delegated to inner effect`() {
        val inner = FakeOverscrollEffect()
        val wrapped = inner.withoutFlingBounce()
        val delta = Offset(0f, 42f)

        val consumed = wrapped.applyToScroll(delta, NestedScrollSource.UserInput) { it }

        assertEquals(delta, inner.scrollDelta)
        assertEquals(NestedScrollSource.UserInput, inner.scrollSource)
        assertEquals(delta, consumed)
    }

    @Test
    fun `Given wrapped effect, When node is accessed, Then inner node is exposed`() {
        val inner = FakeOverscrollEffect()
        val wrapped = inner.withoutFlingBounce()

        assertSame(inner.node, wrapped.node)
    }
}
