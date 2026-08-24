package io.jacob.episodive.core.player.audio

import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 시계를 직접 쥐고 돌린다. 지연 보정이 "잰 시각으로부터 얼마나 지났는가" 로 판정하므로,
 * 실제 시계에 맡기면 무엇이 언제 나오는지 검증할 수 없다.
 */
class PlaybackAmplitudeMonitorTest {
    private var now = 0L
    private val monitor = PlaybackAmplitudeMonitor(nanoTime = { now })

    private fun advance(millis: Long) {
        now += millis * 1_000_000L
    }

    /** 진폭 [peak](0..1)인 사인파 한 덩어리. */
    private fun tone(peak: Float, frames: Int = 2048): ByteBuffer =
        ByteBuffer.allocate(frames * 2).order(ByteOrder.LITTLE_ENDIAN).apply {
            repeat(frames) { index ->
                val value = peak * sin(2.0 * Math.PI * index / 64.0)
                putShort((value * Short.MAX_VALUE).roundToInt().toShort())
            }
            flip()
        }

    private fun silence(frames: Int = 2048): ByteBuffer = tone(peak = 0f, frames = frames)

    /**
     * 버퍼를 계속 밀어넣으며 시간을 흘린다. 한 번에 지연을 건너뛰면 큐에 표본이 하나뿐이라
     * 실제 재생과 다른 조건이 된다.
     */
    private fun feed(buffer: () -> ByteBuffer, millis: Long, stepMillis: Long = 20) {
        var fed = 0L
        while (fed < millis) {
            monitor.handleBuffer(buffer())
            advance(stepMillis)
            fed += stepMillis
        }
    }

    @Test
    fun `Given 16bit PCM, When loud audio is fed past the latency, Then amplitude rises`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { tone(peak = 0.5f) }, millis = 800)

        assertTrue(
            "큰 소리를 지연 시간 넘게 넣었으면 크기가 올라와야 한다: ${monitor.amplitude.value}",
            monitor.amplitude.value > 0.5f,
        )
    }

    @Test
    fun `Given loud audio, When latency has not passed yet, Then nothing is published`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        // 지연(0.39초)보다 짧게만 넣는다. 잰 소리는 아직 귀에 닿지 않았다.
        feed(buffer = { tone(peak = 0.5f) }, millis = 300)

        assertEquals(0f, monitor.amplitude.value, 0f)
    }

    @Test
    fun `Given silence, When fed past the latency, Then amplitude stays at zero`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { silence() }, millis = 800)

        assertEquals(0f, monitor.amplitude.value, 0.01f)
    }

    @Test
    fun `Given loud audio, When it goes silent, Then amplitude falls back`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.5f) }, millis = 800)
        val loud = monitor.amplitude.value

        feed(buffer = { silence() }, millis = 1_500)

        assertTrue("조용해지면 크기가 내려가야 한다: $loud -> ${monitor.amplitude.value}", monitor.amplitude.value < loud)
    }

    @Test
    fun `Given non 16bit encoding, When audio is fed, Then it is ignored`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_FLOAT)

        feed(buffer = { tone(peak = 0.9f) }, millis = 800)

        assertEquals(0f, monitor.amplitude.value, 0f)
    }

    @Test
    fun `Given a raised amplitude, When reset is called, Then it drops to zero at once`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.5f) }, millis = 800)
        assertTrue(monitor.amplitude.value > 0f)

        monitor.reset()

        assertEquals(0f, monitor.amplitude.value, 0f)
    }

    @Test
    fun `Given reset while playing, When audio resumes, Then the stale queue is not published`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.5f) }, millis = 800)
        monitor.reset()

        // 되돌린 직후에는 큐가 비어 있어야 한다. 남아 있으면 예전 소리가 바로 튀어나온다.
        feed(buffer = { silence() }, millis = 100)

        assertEquals(0f, monitor.amplitude.value, 0f)
    }
}
