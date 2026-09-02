package io.jacob.episodive.core.player.audio

import androidx.media3.common.C
import io.jacob.episodive.core.model.Spectrum
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 시계를 직접 쥐고 돌린다. 지연 보정이 "잰 시각으로부터 얼마나 지났는가" 로 판정하므로,
 * 실제 시계에 맡기면 무엇이 언제 나오는지 검증할 수 없다.
 *
 * 이 파일은 프로젝트 규칙(Flow 는 Turbine)의 의도적 예외로 `spectrum.value` 를 직접 본다.
 * `spectrum` 을 구독하면 [PlaybackSpectrumMonitor] 의 `subscriptionCount` 게이트가 열려
 * 백그라운드 분석 루프(`Dispatchers.Default`, 실제 시계)가 이 테스트가 손으로 돌리는
 * [PlaybackSpectrumMonitor.analyzeOnce] 와 같은 프레임을 두고 경합한다. `.value` 로만 읽으면
 * 구독자가 0 이라 그 루프는 애초에 시작조차 하지 않는다.
 */
class PlaybackSpectrumMonitorTest {
    private var now = 0L
    private val monitor = PlaybackSpectrumMonitor(nanoTime = { now }, dispatcher = Dispatchers.Unconfined)
    private val analyzer = FftSpectrumAnalyzer()

    private fun advance(millis: Long) {
        now += millis * 1_000_000L
    }

    /**
     * 단일 사인파 한 덩어리를 프레임 단위로 채널 수만큼 반복 기록한다.
     *
     * [PlaybackSpectrumMonitor.handleBuffer] 는 이제 채널을 평균해 모노로 접으므로, 채널마다
     * 다른 값을 넣으면(예전처럼 채널 개념 없이 표본을 늘어놓으면) 다운믹스 후 실효 주파수가
     * 절반이 되어 밴드 분리 테스트가 통째로 잘못된 밴드를 가리킨다.
     */
    private fun tone(
        peak: Float,
        hz: Float = 689f,
        frames: Int = 2048,
        channels: Int = 2,
        sampleRateHz: Int = DefaultSampleRateHz,
    ): ByteBuffer = frameBuffer(frames, channels) { frameIndex ->
        (peak * sin(2.0 * Math.PI * hz * frameIndex / sampleRateHz)).toFloat()
    }

    /** 서로 다른 주파수 성분을 겹쳐 넣는다(동시 상승 테스트용). */
    private fun composite(
        components: List<Pair<Float, Float>>,
        frames: Int = 2048,
        channels: Int = 2,
        sampleRateHz: Int = DefaultSampleRateHz,
    ): ByteBuffer = frameBuffer(frames, channels) { frameIndex ->
        var sum = 0.0
        for ((hz, peak) in components) {
            sum += peak * sin(2.0 * Math.PI * hz * frameIndex / sampleRateHz)
        }
        sum.toFloat()
    }

    /**
     * 하모닉 스택(n=1..harmonics, 진폭 1/n)을 목표 dBFS 로 스케일해 채운다. RNG 를 쓰지 않고
     * 정수 하모닉만으로 결정적으로 만든다 — 같은 인자면 항상 같은 파형이 나와야 실측 임계값이
     * 재현 가능하다.
     */
    private fun harmonicStack(
        fundamentalHz: Float,
        harmonics: Int,
        targetDbfs: Float,
        frames: Int = 2048,
        channels: Int = 2,
        sampleRateHz: Int = DefaultSampleRateHz,
    ): ByteBuffer {
        val raw = DoubleArray(frames) { frameIndex ->
            var sum = 0.0
            for (n in 1..harmonics) {
                sum += (1.0 / n) * sin(2.0 * Math.PI * fundamentalHz * n * frameIndex / sampleRateHz)
            }
            sum
        }
        val rms = sqrt(raw.sumOf { it * it } / raw.size)
        val targetRms = Math.pow(10.0, targetDbfs / 20.0)
        val scale = targetRms / rms
        return frameBuffer(frames, channels) { frameIndex -> (raw[frameIndex] * scale).toFloat() }
    }

    private fun frameBuffer(frames: Int, channels: Int, amplitudeAt: (Int) -> Float): ByteBuffer =
        ByteBuffer.allocate(frames * channels * 2).order(ByteOrder.LITTLE_ENDIAN).apply {
            repeat(frames) { frameIndex ->
                val sample = (amplitudeAt(frameIndex).coerceIn(-1f, 1f) * Short.MAX_VALUE).roundToInt().toShort()
                repeat(channels) { putShort(sample) }
            }
            flip()
        }

    private fun silence(frames: Int = 2048, channels: Int = 2): ByteBuffer =
        tone(peak = 0f, frames = frames, channels = channels)

    /**
     * 버퍼를 계속 밀어넣으며 시간을 흘린다. 한 번에 지연을 건너뛰면 큐에 표본이 하나뿐이라
     * 실제 재생과 다른 조건이 된다.
     *
     * `handleBuffer` 뒤에 `analyzeOnce()` 를 한 번 더 부른다 — 실제로는 33ms 마다 도는 분석
     * 코루틴이 하는 일을, 시계를 손으로 쥔 이 테스트가 대신 한 틱씩 불러 준다.
     */
    private fun feed(buffer: () -> ByteBuffer, millis: Long, stepMillis: Long = 20) {
        var fed = 0L
        while (fed < millis) {
            monitor.handleBuffer(buffer())
            advance(stepMillis)
            monitor.analyzeOnce()
            fed += stepMillis
        }
    }

    private fun levels(): List<Float> = monitor.spectrum.value.levels

    @Test
    fun `Given 16bit PCM, When loud audio is fed past the latency, Then its band rises`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { tone(peak = 0.5f) }, millis = 800)

        val band = bandFor(689f)
        assertTrue(
            "689Hz 큰 소리를 지연 시간 넘게 넣었으면 그 밴드가 올라와야 한다: ${levels()}",
            levels()[band] > 0.5f,
        )
    }

    @Test
    fun `Given loud audio, When latency has not passed yet, Then nothing is published`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        // 지연(0.39초)보다 짧게만 넣는다. 잰 소리는 아직 귀에 닿지 않았다.
        feed(buffer = { tone(peak = 0.5f) }, millis = 300)

        assertEquals(Spectrum.Silent.levels, levels())
    }

    @Test
    fun `Given silence, When fed past the latency, Then every band stays near zero`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { silence() }, millis = 800)

        levels().forEach { assertTrue("무음인데 값이 남아 있다: ${levels()}", it <= 0.01f) }
    }

    @Test
    fun `Given loud audio, When it goes silent, Then the loud band falls back`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.5f) }, millis = 800)
        val band = bandFor(689f)
        val loud = levels()[band]

        feed(buffer = { silence() }, millis = 1_500)

        assertTrue(
            "조용해지면 그 밴드가 내려가야 한다: $loud -> ${levels()[band]}",
            levels()[band] < loud,
        )
    }

    @Test
    fun `Given non 16bit encoding, When audio is fed, Then it is ignored`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_FLOAT)

        feed(buffer = { tone(peak = 0.9f) }, millis = 800)

        assertEquals(Spectrum.Silent.levels, levels())
    }

    @Test
    fun `Given a raised band, When reset is called, Then it drops to silent at once`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.5f) }, millis = 800)
        assertTrue(levels().any { it > 0f })

        monitor.reset()

        assertEquals(Spectrum.Silent.levels, levels())
    }

    @Test
    fun `Given reset while playing, When audio resumes, Then the stale queue is not published`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.5f) }, millis = 800)
        monitor.reset()

        // 되돌린 직후에는 큐가 비어 있어야 한다. 남아 있으면 예전 소리가 바로 튀어나온다.
        feed(buffer = { silence() }, millis = 100)

        assertEquals(Spectrum.Silent.levels, levels())
    }

    @Test
    fun `Given a stop, When no more buffers arrive, Then bands never re-light from stale frames`() {
        // 설계안 1 검증 중대1 / 설계안 3 검증 C1: reset() 이 readyFrame 을 비우지 않으면,
        // 정지 순간 창 하나가 아직 analyzeOnce() 에 소비되지 않은 채로 남아 있다가 그 뒤에
        // 분석돼 pending 큐에 실린다. 그 큐는 다음 실제 프레임이 analyzeOnce() 를 한 번 더
        // 통과시켜야만 비워지므로(빈 손이면 analyzeOnce() 가 그 자리에서 바로 돌아간다),
        // 이어서 무음이 들어오는 도중 지연 시간(0.39초)이 지난 시점에 옛 소리가 한 번
        // 튀어나온다 — "잠잠해진 지 0.4초 뒤에 옛 소리가 튀어나온다"는 그 버그다.
        //
        // feed() 는 handleBuffer 마다 analyzeOnce() 를 함께 부르므로 그 경로로는 창이 항상
        // 곧바로 비워져 이 결함이 드러나지 않는다. 그래서 마지막 한 번은 analyzeOnce() 를
        // 거치지 않은 채로 reset() 을 불러 정지 직전 readyFrame 에 소리가 남아 있는 상황을
        // 만들고, 곧바로 analyzeOnce() 를 한 번 더 불러(분석 루프의 다음 틱이 다음
        // handleBuffer 보다 먼저 돌 수도 있는 실제 타이밍) 그 창을 pending 에 태운다.
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.9f) }, millis = 800)
        assertTrue(levels().any { it > 0f })

        monitor.handleBuffer(tone(peak = 0.9f))
        monitor.reset()
        monitor.analyzeOnce()

        var fed = 0L
        while (fed < 1_000) {
            monitor.handleBuffer(silence())
            advance(20)
            monitor.analyzeOnce()
            assertTrue(
                "무음이 흐르는 중에 정지 전 옛 소리가 튀어나왔다: ${levels()}",
                levels().all { it <= 0.01f },
            )
            fed += 20
        }
    }

    @Test
    fun `Given a stop, When the loop keeps ticking with no audio at all, Then nothing lights up`() {
        // 위 테스트는 검증하는 내내 무음 버퍼를 계속 먹인다. 그래서 창을 소비하지 않는 구현
        // (링버퍼 회귀)도 늘 갓 들어온 무음 창을 다시 보게 되어 값이 떨어지고, 그 회귀가
        // 초록으로 지나간다. 여기서는 정지 뒤 **버퍼를 한 번도 넣지 않고** 분석 틱만 돌린다 —
        // 실제 정지 상태(handleBuffer 가 더는 불리지 않는다)와 같은 조건이다.
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.9f) }, millis = 800)
        assertTrue(levels().any { it > 0f })

        monitor.handleBuffer(tone(peak = 0.9f))
        monitor.reset()

        repeat(30) {
            advance(33)
            monitor.analyzeOnce()
            assertTrue(
                "정지 뒤 새 소리가 없는데 막대가 다시 켜졌다: ${levels()}",
                levels().all { it == 0f },
            )
        }
    }

    @Test
    fun `Given a window that lands right after a reset, When the loop wakes up later, Then it stays silent`() {
        // reset() 이 창을 버린 **뒤에** 오디오 스레드가 마저 채운 창은 그대로 인계 자리에 남는다.
        // 그동안 분석 루프는 구독이 끊겨 멎어 있으므로 아무도 가져가지 않고, 다시 클립 탭에
        // 들어와 루프가 깨어나는 순간 그 옛 창이 첫 값이 된다 — 잰 지 한참 지난 창이라 지연
        // 게이트를 그대로 통과해, 정지 상태인데 막대가 옛 소리로 튀어 오른 채 굳는다.
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feed(buffer = { tone(peak = 0.9f) }, millis = 800)
        monitor.reset()

        monitor.handleBuffer(tone(peak = 0.9f))
        advance(5_000)
        monitor.analyzeOnce()

        assertEquals(Spectrum.Silent.levels, levels())
    }

    @Test
    fun `Given a reset that lands mid-analysis, When the tick reaches the publish point, Then it stands down`() {
        // reset() 은 분석이 창을 집어 든 **뒤에도** 도착한다(정지를 알리는 것은 다른 스레드다).
        // 그 호출이 진입 시점의 플래그만 보고 그대로 내보내면, reset 이 쓴 잠잠함을 정지 직전
        // 소리가 덮어쓴다 — 그 뒤로는 버퍼가 오지 않아 아무도 되돌리지 못해 막대 다섯이 켜진
        // 채로 굳는다.
        //
        // 한 스레드로는 그 순간을 만들 수 없으므로 시계를 이음매로 쓴다. analyzeOnce() 가
        // 발행 직전에 시각을 한 번 읽는데, 바로 그 자리에서 reset() 을 부른다.
        lateinit var target: PlaybackSpectrumMonitor
        var armed = false
        target = PlaybackSpectrumMonitor(
            nanoTime = {
                if (armed) {
                    armed = false
                    target.reset()
                }
                now
            },
            dispatcher = Dispatchers.Unconfined,
        )
        target.flush(44_100, 2, C.ENCODING_PCM_16BIT)
        feedInto(target, buffer = { tone(peak = 0.9f) }, millis = 800)
        assertTrue(target.spectrum.value.levels.any { it > 0f })

        target.handleBuffer(tone(peak = 0.9f))
        advance(50)
        armed = true
        target.analyzeOnce()

        assertEquals(Spectrum.Silent.levels, target.spectrum.value.levels)
    }

    @Test
    fun `Given the same tone in each band, When fed, Then the band gains shift the levels as specified`() {
        // 밴드 게인은 실기기 튜닝이 필요한 가장 약한 고리인데, 어떤 톤 테스트도 이 값을 고정하지
        // 못한다(전부 0 으로 지워도 초록이다). 밴드마다 같은 크기의 톤을 넣으면 밴드 RMS 가 같으니
        // level 차이는 곧 게인 차이(dB / 26dB 창)여야 한다. 아래 기대값은 지금 게인
        // [6,5,7,12,18]dB 를 밴드0 기준 차이로 돌려 26dB 창으로 나눈 것이다 — 게인을 바꾸면
        // 여기도 함께 고쳐야 한다(그게 이 테스트의 값이다).
        //
        // 톤은 밴드마다 빈 중심에 놓아(4·8·16·32·96번 빈) 이웃 밴드로 새지 않게 한다. 크기는
        // 밴드 RMS -38dB(≈0.0126)으로 잡는다. 게인이 커지면서 예전 크기(-30.5dB)로는 가장 큰
        // 게인(18dB)을 얹은 밴드4 가 -12.5dB 로 창 위(-14dB)를 넘어 포화해, 게인 차이가 눌려
        // 사라졌다. -38dB 이면 밴드4 가 -20dB(level 0.77), 가장 작은 게인(5dB)을 얹은 밴드1 이
        // -33dB(level 0.27)로 다섯이 모두 창 -40..-14dB 안에 들어와 차이가 그대로 보인다.
        val binHz = DefaultSampleRateHz / 1024f
        val peak = 0.0126f * sqrt(2.0).toFloat()
        val expectedGap = floatArrayOf(0f, -1f / 26f, 1f / 26f, 6f / 26f, 12f / 26f)

        val measured = intArrayOf(4, 8, 16, 32, 96).mapIndexed { band, bin ->
            val isolated = PlaybackSpectrumMonitor(nanoTime = { now }, dispatcher = Dispatchers.Unconfined)
            isolated.flush(44_100, 2, C.ENCODING_PCM_16BIT)
            feedInto(isolated, buffer = { tone(peak = peak, hz = bin * binHz) }, millis = 800)
            isolated.spectrum.value.levels[band]
        }

        for (band in measured.indices) {
            assertEquals(
                "밴드 $band 의 게인이 명세와 다르다: $measured",
                expectedGap[band].toDouble(),
                (measured[band] - measured[0]).toDouble(),
                0.03,
            )
        }
    }

    @Test
    fun `Given single tones in each band, When fed, Then only their own band rises`() {
        // 경계 근처 톤(200 260 1190Hz)은 두 밴드에 걸치므로 여기 쓰지 않는다.
        //
        // 톤 크기는 0.05(밴드 RMS -29dBFS)다. 예전의 0.5(-9dBFS)짜리 순음은 실제 재생에 나오지
        // 않는 크기인 데다, 그만큼 크면 Hann 창의 사이드로브 누설까지 26dB 창 안으로 끌려 올라와
        // 이웃 밴드가 0.08 로 켜졌다. 누설은 톤 대비 고정 비율이므로 톤을 낮추면 함께 내려간다 —
        // 임계 0.05 와 자기 밴드 > 0.5 는 그대로 두고 신호만 실제 재생 수준으로 옮긴 것이다.
        val cases = listOf(150f to 0, 689f to 2, 1378f to 3, 4000f to 4)

        for ((hz, expectedBand) in cases) {
            val isolated = PlaybackSpectrumMonitor(nanoTime = { now }, dispatcher = Dispatchers.Unconfined)
            isolated.flush(44_100, 2, C.ENCODING_PCM_16BIT)
            feedInto(isolated, buffer = { tone(peak = 0.05f, hz = hz) }, millis = 800)

            val result = isolated.spectrum.value.levels
            assertTrue(
                "${hz}Hz 는 밴드 $expectedBand 에 실려야 한다: $result",
                result[expectedBand] > 0.5f,
            )
            result.forEachIndexed { band, level ->
                if (band == expectedBand) return@forEachIndexed
                assertTrue(
                    "${hz}Hz 가 엉뚱한 밴드 $band 까지 새어 나갔다: $result",
                    level < 0.05f,
                )
            }
        }
    }

    @Test
    fun `Given two tones at opposite ends, When fed, Then both bands rise together`() {
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { composite(listOf(150f to 0.35f, 4000f to 0.35f)) }, millis = 800)

        val result = levels()
        assertTrue("저역(밴드0)이 함께 올라와야 한다: $result", result[0] > 0.4f)
        assertTrue("치찰음(밴드4)도 함께 올라와야 한다: $result", result[4] > 0.4f)
        assertTrue("가운데(밴드2)는 낮게 남아야 한다: $result", result[2] < 0.3f)
    }

    @Test
    fun `Given a harmonic-rich tone, When fed, Then no band saturates and bands differ`() {
        // 120Hz 하모닉 스택(n=1..80, 진폭 1/n), RMS -28dBFS. 전 밴드가 1.0 으로 포화되거나
        // 전부 같은 값을 내는 구현이면 아래 두 단언 중 하나가 반드시 깨진다.
        //
        // 크기를 -20dBFS 에서 -28dBFS 로 내린 것은 게인이 커지면서 위쪽이 창 천장에 눌렸기
        // 때문이다(-20dBFS 에서는 밴드0 이 0.938 까지 올라 포화 단언의 여유가 0.01 밖에
        // 남지 않았다). 지금 실측값은 [0.630, 0.373, 0.318, 0.382, 0.461](편차 0.313)로
        // 다섯이 모두 창 한가운데에 있다. 아래 두 임계(0.05..0.95, 스프레드 0.15)는 그
        // 실측값에서 양쪽으로 넉넉히 여유를 두고도 "전부 1.0" · "전 밴드 동일" 두 결함
        // 구현을 가려낸다.
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { harmonicStack(fundamentalHz = 120f, harmonics = 80, targetDbfs = -28f) }, millis = 800)

        val result = levels()
        result.forEach {
            assertTrue("포화(1.0 붙박이)되거나 완전히 죽은 밴드가 있다: $result", it in 0.05f..0.95f)
        }
        val spread = (result.max() - result.min())
        assertTrue("전 밴드가 같은 값을 낸다(밴드 분리가 무의미해진다): $result", spread > 0.15f)
        // 스프레드만으로는 부족하다 — 밴드 게인 [6,5,7,12,18] 이 그 자체로 0.5 의 편차를 만들어,
        // 밴드를 전체 RMS 로 접어 버린 구현도 스프레드를 통과한다. 저역 우세 하모닉 스택이니
        // 방향까지 못 박는다: 접힌 구현은 다섯 밴드가 같은 RMS 를 보므로 이 차이가 순전히
        // 게인 차이 (6-18)/26 = -0.46, 즉 **음수**가 된다. 실측 차이는 0.170 이라(게인이
        // 커지며 위 밴드가 따라 올라와 예전 0.323 보다 좁아졌다) 임계를 그 아래 0.10 으로
        // 두어 여유를 남기되, 0 보다는 확실히 위에 놓아 접힌 구현을 걸러 낸다.
        assertTrue(
            "저역 우세 신호인데 밴드0 이 밴드4 를 앞서지 못한다(밴드 폴딩 의심): $result",
            result[0] - result[4] > 0.10f,
        )
    }

    @Test
    fun `Given only sibilant-band energy, When fed, Then the top band leads the low band`() {
        // 3~8kHz 대역에만 에너지를 몰아 넣는다. 하모닉 합으로 결정적으로 만든다(RNG 금지).
        val sibilant = listOf(3500f, 4200f, 5000f, 6000f, 7000f, 7800f).map { it to (1f / 6f) }
        monitor.flush(44_100, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { composite(sibilant, frames = 2048) }, millis = 800)

        val result = levels()
        assertTrue(
            "치찰음만 있는 신호인데 저역이 치찰음 밴드보다 뒤처지지 않는다: $result",
            result[4] > result[0] + 0.3f,
        )
    }

    @Test
    fun `Given a different sample rate, When the same tone is fed, Then the same band leads`() {
        monitor.flush(48_000, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { tone(peak = 0.5f, hz = 689f, sampleRateHz = 48_000) }, millis = 800)

        val result = levels()
        val expectedBand = bandFor(689f)
        assertTrue(
            "샘플레이트가 바뀌어도 같은 주파수는 같은 밴드에 실려야 한다: $result",
            result[expectedBand] > 0.5f,
        )
    }

    @Test
    fun `Given a 16kHz stream, When a 400Hz tone is fed, Then the rate on the frame decides the band`() {
        // 48kHz 만으로는 계약이 서지 않는다 — 689Hz 는 44.1k 맵에서도 48k 맵에서도 밴드2 라,
        // 레이트를 44_100 으로 못 박은 구현이 그대로 통과한다. 16kHz 의 400Hz 는 제 맵에서
        // 밴드1(빈 16..31)이지만 44.1k 맵으로 읽으면 밴드2 로 올라가므로 둘이 갈린다.
        monitor.flush(16_000, 2, C.ENCODING_PCM_16BIT)

        feed(buffer = { tone(peak = 0.5f, hz = 400f, sampleRateHz = 16_000) }, millis = 800)

        val result = levels()
        assertEquals(
            "창에 실려 온 레이트로 밴드를 지어야 한다: $result",
            1,
            result.indices.maxBy { result[it] },
        )
    }

    @Test
    fun `Given a mono stream, When the same tone is fed, Then the same band leads`() {
        monitor.flush(44_100, 1, C.ENCODING_PCM_16BIT)

        feed(buffer = { tone(peak = 0.5f, hz = 689f, channels = 1) }, millis = 800)

        val result = levels()
        val expectedBand = bandFor(689f)
        assertTrue(
            "모노에서도 같은 주파수는 같은 밴드에 실려야 한다: $result",
            result[expectedBand] > 0.5f,
        )
    }

    /** 이 주파수가 44.1kHz 기준으로 속하는 밴드 번호. 경계 계산을 손으로 베끼지 않는다. */
    private fun bandFor(hz: Float): Int =
        analyzer.bandBinsFor(DefaultSampleRateHz).indexOfFirst { bins ->
            !bins.isEmpty() && bins.first * BinHz44100 <= hz && hz < (bins.last + 1) * BinHz44100
        }

    /** `feed()` 는 이 파일의 [monitor] 전용이라, 다른 인스턴스로 돌릴 때는 이 함수를 쓴다. */
    private fun feedInto(target: PlaybackSpectrumMonitor, buffer: () -> ByteBuffer, millis: Long, stepMillis: Long = 20) {
        var fed = 0L
        while (fed < millis) {
            target.handleBuffer(buffer())
            advance(stepMillis)
            target.analyzeOnce()
            fed += stepMillis
        }
    }

    private companion object {
        const val DefaultSampleRateHz = 44_100
        const val BinHz44100 = DefaultSampleRateHz / 1024f
    }
}
