package io.jacob.episodive.core.player.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

/**
 * 순수 신호 처리 로직만 검증한다. 오디오 스레드도 코루틴도 끼어들지 않으므로 실제 시계·
 * `handleBuffer` 없이 [FftSpectrumAnalyzer.bandRms] 를 직접 호출한다.
 */
class FftSpectrumAnalyzerTest {
    private val analyzer = FftSpectrumAnalyzer()

    /** 지정한 샘플레이트에서 [freqHz] 짜리 순수 사인파를 [FftSpectrumAnalyzer.DefaultFftSize] 길이만큼 만든다. */
    private fun sineSamples(
        sampleRateHz: Int,
        freqHz: Float,
        peak: Float = 1f,
        size: Int = FftSpectrumAnalyzer.DefaultFftSize,
    ): FloatArray = FloatArray(size) { index ->
        (peak * sin(2.0 * PI * freqHz * index / sampleRateHz)).toFloat()
    }

    @Test
    fun `Given a sample rate, When asking for band bins, Then edges match the reference table`() {
        assertEquals(
            listOf(2..5, 6..11, 12..27, 28..69, 70..185),
            analyzer.bandBinsFor(44_100),
        )
        assertEquals(
            listOf(2..5, 6..10, 11..25, 26..63, 64..170),
            analyzer.bandBinsFor(48_000),
        )
        // 낮은 레이트에서는 최상위 밴드의 끝이 Nyquist 로 잘린다. 16kHz 는 마침 클램프가
        // 아무것도 자르지 않는 지점(끝이 정확히 511)이라 이 경로를 덮지 못한다 — 클램프를
        // 지워도 초록으로 지나가므로, 실제로 잘리는 11.025kHz 로 못 박는다. 자르지 않으면
        // 미러 절반을 이중으로 세고, 더 낮은 레이트에서는 배열 밖을 읽는다.
        assertEquals(279..511, analyzer.bandBinsFor(11_025).last())
    }

    @Test
    fun `Given a full scale 689Hz tone, When analyzed, Then only band 2 responds`() {
        val samples = sineSamples(sampleRateHz = 44_100, freqHz = 689f)
        val out = FloatArray(FftSpectrumAnalyzer.BandCount)

        analyzer.bandRms(samples, 44_100, out)

        assertEquals("풀스케일 689Hz 사인의 밴드2 RMS", 0.7068f, out[2], 0.02f)
        for (band in intArrayOf(0, 1, 3, 4)) {
            assertTrue(
                "밴드 $band 는 낮아야 한다: ${out[band]}",
                out[band] < 1e-3f,
            )
        }
    }

    @Test
    fun `Given silence, When analyzed, Then every band is effectively zero`() {
        val samples = FloatArray(FftSpectrumAnalyzer.DefaultFftSize) { 0f }
        val out = FloatArray(FftSpectrumAnalyzer.BandCount)

        analyzer.bandRms(samples, 44_100, out)

        for (band in 0 until FftSpectrumAnalyzer.BandCount) {
            assertTrue("밴드 $band 는 무음이어야 한다: ${out[band]}", out[band] < 1e-6f)
        }
    }

    @Test
    fun `Given a DC input, When analyzed, Then every band stays near zero but not exactly zero`() {
        val samples = FloatArray(FftSpectrumAnalyzer.DefaultFftSize) { 0.5f }
        val out = FloatArray(FftSpectrumAnalyzer.BandCount)

        analyzer.bandRms(samples, 44_100, out)

        // 빈 0(DC)은 어느 밴드에도 들지 않으므로 새어 나오는 양은 작지만, 정확히 0은 아니다.
        for (band in 0 until FftSpectrumAnalyzer.BandCount) {
            assertTrue("밴드 $band 는 작아야 한다: ${out[band]}", out[band] < 1e-3f)
        }
    }

    @Test
    fun `Given a tone above the top band edge, When analyzed, Then every band stays near zero`() {
        val samples = sineSamples(sampleRateHz = 44_100, freqHz = 12_000f)
        val out = FloatArray(FftSpectrumAnalyzer.BandCount)

        analyzer.bandRms(samples, 44_100, out)

        for (band in 0 until FftSpectrumAnalyzer.BandCount) {
            assertTrue("밴드 $band 는 작아야 한다: ${out[band]}", out[band] < 1e-3f)
        }
    }

    @Test
    fun `Given a low sample rate, When a 3_5kHz tone is analyzed, Then band 4 responds without throwing`() {
        val samples = sineSamples(sampleRateHz = 16_000, freqHz = 3_500f)
        val out = FloatArray(FftSpectrumAnalyzer.BandCount)

        analyzer.bandRms(samples, 16_000, out)

        val maxBand = out.indices.maxBy { out[it] }
        assertEquals("최댓값은 밴드4 여야 한다: ${out.toList()}", 4, maxBand)
    }

    @Test
    fun `Given the same instance, When called repeatedly, Then results stay identical`() {
        val samples = sineSamples(sampleRateHz = 44_100, freqHz = 689f)
        val first = FloatArray(FftSpectrumAnalyzer.BandCount)
        analyzer.bandRms(samples, 44_100, first)

        repeat(100) {
            val out = FloatArray(FftSpectrumAnalyzer.BandCount)
            analyzer.bandRms(samples, 44_100, out)
            assertEquals(first.toList(), out.toList())
        }
    }
}
