package io.jacob.episodive.core.player.audio

import androidx.media3.common.C
import io.jacob.episodive.core.model.Spectrum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 구독 게이트와 33ms 분석 루프만 본다.
 *
 * [PlaybackSpectrumMonitorTest] 는 결정적인 시계를 지키려고 일부러 구독하지 않고
 * [PlaybackSpectrumMonitor.analyzeOnce] 를 손으로 부른다. 그래서 정작 실제 구동 경로 — 구독이
 * 있을 때만 루프를 돌리는 게이트와 루프 자체 — 는 그 파일에서 한 줄도 실행되지 않아, 게이트
 * 판정을 뒤집어도 전부 초록이었다. 여기서는 디스패처까지 가상 시계에 묶어 그 경로를 돌린다.
 *
 * `runTest` 를 쓰지 않는 것은 그 뒷정리가 남은 가상 시각을 끝까지 밀기 때문이다. 게이트가
 * 뒤집혀 구독 없이도 루프가 돌면 그 뒷정리가 끝나지 않는 루프를 만나 **실패 대신 멈춤**이
 * 된다. 스케줄러를 직접 쥐면 그 회귀도 그냥 단언 실패로 드러난다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackSpectrumMonitorGateTest {
    private var now = 0L
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)
    private val collectScope = CoroutineScope(dispatcher)

    @After
    fun tearDown() {
        collectScope.cancel()
    }

    @Test
    fun `Given no subscriber, When audio is fed, Then the analysis loop never runs`() {
        val monitor = newMonitor()

        pump(monitor, millis = 1_000)

        assertEquals(
            "아무도 보지 않는 동안에는 FFT 가 돌지 않아야 한다",
            Spectrum.Silent.levels,
            monitor.spectrum.value.levels,
        )
    }

    @Test
    fun `Given a subscriber, When it comes and goes, Then the loop follows it`() {
        val monitor = newMonitor()
        pump(monitor, millis = 1_000)

        val subscription = collectScope.launch { monitor.spectrum.collect {} }
        scheduler.runCurrent()
        pump(monitor, millis = 1_000)

        val levels = monitor.spectrum.value.levels
        assertTrue("구독이 열리면 루프가 돌아 값이 올라와야 한다: $levels", levels.any { it > 0.5f })

        subscription.cancel()
        scheduler.runCurrent()
        val frozen = monitor.spectrum.value.levels
        pump(monitor, millis = 1_000)

        assertEquals(
            "구독이 끊기면 루프도 멎어야 한다(계속 돌면 배터리만 먹는다)",
            frozen,
            monitor.spectrum.value.levels,
        )
    }

    private fun newMonitor(): PlaybackSpectrumMonitor =
        PlaybackSpectrumMonitor(nanoTime = { now }, dispatcher = dispatcher).apply {
            flush(44_100, 2, C.ENCODING_PCM_16BIT)
        }

    /** 소리를 흘려보내며 가상 시계와 모니터의 시계를 함께 민다. */
    private fun pump(monitor: PlaybackSpectrumMonitor, millis: Long, stepMillis: Long = 20) {
        repeat((millis / stepMillis).toInt()) {
            monitor.handleBuffer(tone())
            now += stepMillis * 1_000_000L
            scheduler.advanceTimeBy(stepMillis)
            scheduler.runCurrent()
        }
    }

    private fun tone(peak: Float = 0.5f, hz: Float = 689f, frames: Int = 2048, channels: Int = 2): ByteBuffer =
        ByteBuffer.allocate(frames * channels * 2).order(ByteOrder.LITTLE_ENDIAN).apply {
            repeat(frames) { frameIndex ->
                val value = (peak * sin(2.0 * Math.PI * hz * frameIndex / 44_100)).toFloat()
                val sample = (value.coerceIn(-1f, 1f) * Short.MAX_VALUE).roundToInt().toShort()
                repeat(channels) { putShort(sample) }
            }
            flip()
        }
}
