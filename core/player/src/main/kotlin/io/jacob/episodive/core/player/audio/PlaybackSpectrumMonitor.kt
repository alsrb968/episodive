package io.jacob.episodive.core.player.audio

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import io.jacob.episodive.core.model.Spectrum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.log10

/**
 * 재생 중인 소리를 주파수 대역 다섯 칸으로 나눠 각 칸의 세기를 0..1 로 내보낸다.
 *
 * media3 의 오디오 프로세서 체인에 [TeeAudioProcessor] 로 끼워, 디코딩이 끝나고 `AudioTrack`
 * 으로 넘어가기 직전의 PCM 을 들여다본다. 자기 앱이 디코딩한 자기 오디오라 **`RECORD_AUDIO`
 * 권한이 필요 없다.** (`android.media.audiofx.Visualizer` 는 같은 일을 하면서 마이크 권한을
 * 요구한다 — 막대 다섯 개를 위해 치를 값이 아니다.)
 *
 * 세 가지를 조심한다.
 *
 * 1. [handleBuffer] 는 **오디오 스레드에서** 불린다. 여기서 시간을 끌면 버퍼가 비어 소리가
 *    끊긴다. 그래서 **여기서는 표본을 모노로 접어 담기만 하고, FFT 는 분석 코루틴이 한다.**
 *    담은 창은 [AtomicReference] 한 칸으로 건너가고, 분석이 밀리면 오래된 창이 조용히
 *    버려진다 — 창은 23ms 마다 차는데 분석은 33ms 마다 하나만 가져가므로 **들어온 소리의 약
 *    30% 는 분석되지 않는다.** 눈으로 보는 막대에는 그 차이가 드러나지 않는다.
 * 2. **여기서 보는 것은 지금 들리는 소리가 아니라 곧 들릴 소리다.** 그대로 내보내면 막대가
 *    소리보다 먼저 움직인다. [PlaybackLatencyNanos] 만큼 늦춰 내보내는 이유다.
 * 3. 오디오 오프로드를 켜면 압축 데이터가 DSP 로 직행해 이 경로로 아무것도 오지 않는다.
 *    크래시가 아니라 "막대가 안 움직임" 으로 나타나니, 오프로드를 켜게 되면 이 사실을
 *    함께 적어야 한다.
 */
@Singleton
// 구현하는 인터페이스와 두 오버라이드가 모두 media3 의 opt-in API 다. 함수 하나에만 붙이면
// 나머지가 lintDebug 의 UnsafeOptInUsageError 로 남는다. @UnstableApi 가 아니라 @OptIn 인 것은,
// 이 클래스를 쓰는 쪽까지 unstable 로 번지게 할 이유가 없어서다.
@OptIn(UnstableApi::class)
class PlaybackSpectrumMonitor internal constructor(
    /** 지연 보정이 시계에 기대므로, 테스트가 시간을 쥘 수 있도록 밖에서 받는다. */
    private val nanoTime: NanoClock,
    dispatcher: CoroutineDispatcher,
) : TeeAudioProcessor.AudioBufferSink {
    @Inject
    constructor(
        @Dispatcher(EpisodiveDispatchers.Default) dispatcher: CoroutineDispatcher,
    ) : this(nanoTime = NanoClock { System.nanoTime() }, dispatcher = dispatcher)

    private val _spectrum = MutableStateFlow(Spectrum.Silent)
    val spectrum: StateFlow<Spectrum> = _spectrum.asStateFlow()

    /** [flush] 는 오디오 스레드 밖에서도 불릴 수 있어 [handleBuffer] 와 스레드가 갈린다. */
    @Volatile
    private var pcmEncoding: Int = C.ENCODING_PCM_16BIT

    /** [flush] 가 알려주는 스트림 형식. 예전에는 받고 버렸지만 밴드를 나누려면 둘 다 필요하다. */
    @Volatile
    private var sampleRateHz: Int = DefaultSampleRateHz

    @Volatile
    private var channelCount: Int = 2

    /** 다른 스레드의 [reset] 을 오디오 스레드가 받아 가는 통로. */
    @Volatile
    private var captureResetRequested = false

    /** 같은 [reset] 을 분석 코루틴이 받아 가는 통로. */
    @Volatile
    private var analysisResetRequested = false

    /**
     * [reset] 이 몇 번 지나갔는지. 위 플래그만으로는 **이미 창을 집어 든** 분석을 막지 못한다
     * — 진입할 때 한 번 보고 마니까. 분석은 들어올 때의 값을 적어 두었다가 내보내기 직전에
     * 다시 견줘, 그 사이에 [reset] 이 지나갔으면 자기 결과를 통째로 버린다.
     */
    private val resetGeneration = AtomicInteger(0)

    // ----- 오디오 스레드 전용

    private var captureFrame = AnalysisFrame(FloatArray(FftSize))
    private var captureIndex = 0

    /** 다 찬 창의 인계 지점. 분석이 가져가기 전에 다음 창이 차면 오래된 쪽이 덮여 사라진다. */
    private val readyFrame = AtomicReference<AnalysisFrame?>(null)

    /** 분석이 복사를 마치고 돌려놓는 빈 창. 정상 흐름에서는 이 둘만으로 돌아간다. */
    private val spareFrame = AtomicReference<AnalysisFrame?>(AnalysisFrame(FloatArray(FftSize)))

    // ----- 분석 코루틴 전용 (락이 없는 것은 이 스레드 하나만 만지기 때문이다)

    private val analyzer = FftSpectrumAnalyzer(FftSize)
    private val workSamples = FloatArray(FftSize)
    private val bandRms = FloatArray(Spectrum.BandCount)
    private val smoothed = FloatArray(Spectrum.BandCount)
    private var lastCapturedAt = 0L
    private var lastPublishedAt = 0L
    private var lastLoggedAt = 0L

    /** (잰 시각, 그때의 스펙트럼). 지연 보정을 위해 잠시 쌓아 둔다. */
    private val pending = ArrayDeque<TimedSpectrum>()

    private val analysisScope = CoroutineScope(SupervisorJob() + dispatcher)

    override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
        pcmEncoding = encoding
        this.sampleRateHz = sampleRateHz
        this.channelCount = channelCount
        reset()
    }

    override fun handleBuffer(buffer: ByteBuffer) {
        // 16bit PCM 만 읽는다. float 출력은 기본으로 꺼져 있고, 켜졌다면 조용히 잠잠함을 유지한다.
        if (pcmEncoding != C.ENCODING_PCM_16BIT) return

        // 담던 창을 다른 스레드에서 버리면 오디오 스레드가 그 자리에서 락을 기다리게 된다.
        // 버리라는 표시만 받아 두고, 실제로 버리는 것은 여기 오디오 스레드에서 한다.
        if (captureResetRequested) {
            captureResetRequested = false
            captureIndex = 0
        }

        // 창 하나를 채우는 동안 레이트가 바뀌면 밴드 경계가 창 중간에서 갈린다. 한 번 읽어
        // 두고 창에 함께 실어 보내, 분석 쪽이 그 창을 잰 레이트 그대로 밴드를 짓게 한다.
        val rate = sampleRateHz
        val channels = channelCount.coerceAtLeast(1)
        val frameBytes = channels * BytesPerSample

        // `position` 을 건드리지 않도록 절대 인덱스로 읽는다. 넘어오는 버퍼는 읽기 전용 뷰지만,
        // 이 sink 가 커서를 옮겨도 원본이 무사하다는 보장에 기대지 않는 편이 안전하다.
        var index = buffer.position()
        val end = buffer.limit()
        var samples = captureFrame.samples

        while (index + frameBytes <= end) {
            // 한 프레임의 채널을 평균해 모노로 접는다. 예전처럼 표본을 건너뛰며 읽지 않는다 —
            // 건너뛰기는 유효 Nyquist 를 그만큼 떨어뜨려 고역이 저역 자리로 접혀 들어온다.
            // 크기만 잴 때는 그래도 됐지만 주파수를 가르는 지금은 그 자체가 거짓말이 된다.
            var sum = 0f
            var offset = index
            repeat(channels) {
                // 16bit PCM 은 리틀엔디안이다. 버퍼의 바이트 순서 설정에 기대지 않고 직접 맞춘다.
                val sample = (buffer.get(offset + 1).toInt() shl 8) or
                        (buffer.get(offset).toInt() and 0xFF)
                sum += sample / Short.MAX_VALUE.toFloat()
                offset += BytesPerSample
            }
            samples[captureIndex++] = sum / channels
            index += frameBytes

            if (captureIndex < FftSize) continue

            // 이 호출 도중에 reset 이 도착했으면 방금 채운 창은 정지 이전 소리다. 진입할 때
            // 한 번만 보면 이 창이 그대로 넘어가, 다음 재생의 첫 틱에 옛 소리가 스친다.
            if (captureResetRequested) {
                captureResetRequested = false
                captureIndex = 0
                continue
            }

            val filled = captureFrame
            // 스탬프를 창 끝이 아니라 **창 중심**에 맞춘다. 창 하나는 한 순간이 아니라 23ms 짜리
            // 구간이라, 끝에 맞추면 그만큼 늦은 시각으로 지연 보정에 들어간다.
            filled.capturedAt = nanoTime.nanos() - halfWindowNanos(rate)
            filled.sampleRateHz = rate

            // 분석이 창을 들고 있는 찰나에 양쪽이 모두 비어 보이면 그때만 4KB 를 한 번 만든다.
            // 남의 스레드를 오디오 스레드가 기다리게 두는 것보다 낫다는 판단이다.
            captureFrame = readyFrame.getAndSet(filled)
                ?: spareFrame.getAndSet(null)
                ?: AnalysisFrame(FloatArray(FftSize))
            captureIndex = 0
            samples = captureFrame.samples
        }
    }

    /** 재생이 멎으면 마지막 모양이 그대로 남으므로 직접 되돌린다. */
    fun reset() {
        // 세대를 먼저 올린다. 플래그는 **다음** 분석만 막을 뿐이라, 이미 창을 집어 든 분석은
        // 여기서 쓴 잠잠함을 자기 결과로 덮어쓴다. 그 호출이 내보내기 직전에 세대를 다시 보고
        // 스스로 물러나게 하는 것이 실제로 이 창을 닫는 장치다.
        resetGeneration.incrementAndGet()
        analysisResetRequested = true
        // 넘어가 있던 창을 버린다 — 빠뜨리면 정지 직전의 창이 살아남아, 잠잠해진 지 0.4초 뒤에
        // 옛 소리가 한 번 튀어나온다. 그냥 버리지 않고 빈 자리로 돌려놓는 것은, 그러지 않으면
        // 창 두 개로 돌던 순환에서 하나가 사라져 재개하는 순간 오디오 스레드가 새 배열을
        // 만들어야 하기 때문이다.
        readyFrame.getAndSet(null)?.let { spareFrame.compareAndSet(null, it) }
        captureResetRequested = true
        _spectrum.value = Spectrum.Silent
    }

    /**
     * 넘어온 창 하나를 분석해 필요하면 내보낸다.
     *
     * 33ms 루프가 부르는 것이 본래 자리지만, 테스트가 코루틴 없이 한 틱씩 돌릴 수 있도록
     * `internal` 로 열어 둔다.
     */
    internal fun analyzeOnce() {
        if (analysisResetRequested) {
            analysisResetRequested = false
            // 넘어와 있는 창도 여기서 한 번 더 버린다. [reset] 이 비운 **뒤에** 오디오 스레드가
            // 마저 채워 넘긴 창이 남아 있을 수 있고, 그대로 두면 다시 구독될 때 그 옛 소리가
            // 첫 값으로 튀어 오른 채 굳는다.
            readyFrame.getAndSet(null)?.let { spareFrame.compareAndSet(null, it) }
            pending.clear()
            smoothed.fill(0f)
            lastCapturedAt = 0L
            lastPublishedAt = 0L
        }

        // 세대는 **위 정리를 마친 뒤에** 적는다. 진입 첫 줄에서 읽으면, 바로 앞에 지나간
        // [reset] 의 증가분까지 "내가 도는 동안 지나간 것" 으로 세어 — 그 reset **이후에** 잡힌
        // 멀쩡한 창을 이 틱이 통째로 버린다.
        val generation = resetGeneration.get()

        // 창을 **소비**한다. 새 소리가 없으면 여기서 그대로 돌아가므로, 일시정지 중에는 루프가
        // 계속 돌아도 아무것도 발행되지 않고 [reset] 이 쓴 잠잠함이 유지된다.
        val frame = readyFrame.getAndSet(null) ?: return
        frame.samples.copyInto(workSamples)
        val capturedAt = frame.capturedAt
        val rate = frame.sampleRateHz
        // 복사가 끝나는 즉시 돌려준다. 오디오 스레드가 빈 창을 못 찾는 구간을 최대한 좁힌다.
        spareFrame.set(frame)

        analyzer.bandRms(workSamples, rate, bandRms)

        // 첫 창이거나 reset 직후면 기준 시각이 없다. 이때 스무딩을 태우면 dt 가 0(첫 값이
        // 무시된다)이거나 몇 초(한 번에 점프한다)가 되어 어느 쪽이든 틀린다.
        val hasPrevious = lastCapturedAt != 0L
        val deltaSeconds = (capturedAt - lastCapturedAt)
            .coerceIn(MinDeltaNanos, MaxDeltaNanos) / NanosPerSecond
        lastCapturedAt = capturedAt

        for (band in 0 until Spectrum.BandCount) {
            // 사람 귀는 소리 크기를 로그로 듣는다. 선형 RMS 를 그대로 높이에 쓰면 말소리 대부분이
            // 아래쪽에 눌려붙어 거의 움직이지 않는다.
            val decibels = 20f * log10(bandRms[band].coerceAtLeast(MinimumRms)) +
                    BandGainDecibels[band]
            val level = ((decibels - SilenceDecibels) / (FullScaleDecibels - SilenceDecibels))
                .coerceIn(0f, 1f)

            if (!hasPrevious) {
                smoothed[band] = level
                continue
            }

            // 커질 때는 빠르게 따라가고 잦아들 때는 천천히 놓는다. 둘을 같은 속도로 두면 자음마다
            // 막대가 바닥까지 떨어져 깜빡이는 것처럼 보인다. 발행 간격이 일정하지 않을 수 있으니
            // 고정 비율이 아니라 흐른 시간으로 비율을 구한다.
            val tau = if (level > smoothed[band]) AttackSeconds else ReleaseSeconds[band]
            smoothed[band] += (level - smoothed[band]) * (1f - exp(-deltaSeconds / tau))
        }

        if (LogBandDecibels && capturedAt - lastLoggedAt >= LogIntervalNanos) {
            lastLoggedAt = capturedAt
            Timber.v(
                "band dB: %s",
                bandRms.joinToString { "%.1f".format(20f * log10(it.coerceAtLeast(MinimumRms))) },
            )
        }

        // 창을 집어 든 뒤에 [reset] 이 지나갔다면 이 결과는 정지 이전의 소리다. 쌓지도 내보내지도
        // 않고 물러난다 — 내보내면 [reset] 이 쓴 잠잠함을 덮어써, 더는 버퍼가 오지 않는 정지
        // 상태에서 아무도 그 값을 되돌리지 못해 막대 다섯이 켜진 채로 굳는다.
        if (resetGeneration.get() != generation) return

        // 방금 잰 것은 AudioTrack 에 쌓였다가 잠시 뒤에 들릴 소리다. 잰 시각과 함께 넣어 두고
        // 그만큼 지난 것만 꺼내 내보낸다.
        pending.addLast(TimedSpectrum(measuredAt = capturedAt, spectrum = Spectrum(smoothed.toList())))

        val now = nanoTime.nanos()
        val audibleAt = now - PlaybackLatencyNanos
        var audible: Spectrum? = null
        while (pending.isNotEmpty() && pending.first().measuredAt <= audibleAt) {
            audible = pending.removeFirst().spectrum
        }

        // 실제 지연이 예상보다 큰 기기에서 큐가 끝없이 자라지 않게 막는다.
        while (pending.size > MaxPendingSamples) pending.removeFirst()

        // 화면 갱신에 필요한 만큼만 내보낸다.
        if (audible == null || now - lastPublishedAt < PublishIntervalNanos) return
        if (resetGeneration.get() != generation) return
        lastPublishedAt = now
        _spectrum.value = audible

        // 위 검사와 이 대입은 원자적이지 않다. 그 틈에 [reset] 이 지나가면 방금 쓴 값이 [reset]
        // 의 잠잠함을 덮어써, 더는 버퍼가 오지 않는 정지 상태에서 막대 다섯이 켜진 채로 굳는다.
        // [reset] 은 세대를 **먼저** 올리고 잠잠함을 **나중에** 쓰므로, 여기서 한 번 더 견주면
        // 두 순서 어느 쪽이든 잠잠함이 마지막에 남는다.
        if (resetGeneration.get() != generation) _spectrum.value = Spectrum.Silent
    }

    private suspend fun analysisLoop() {
        while (true) {
            try {
                analyzeOnce()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                // 여기서 예외가 빠져나가면 게이트 콜렉터까지 함께 죽어, 싱글턴이라 프로세스가
                // 사는 내내 막대가 멎는다. 창 하나를 잃는 편이 낫다.
                Timber.e(t, "스펙트럼 분석 한 틱이 실패했다")
            }
            delay(PublishIntervalMillis)
        }
    }

    /** 창의 절반 길이(나노초). 스탬프를 창 중심으로 옮기는 데 쓴다. */
    private fun halfWindowNanos(sampleRateHz: Int): Long =
        FftSize.toLong() * (NanosPerSecondLong / 2) / sampleRateHz.coerceAtLeast(1)

    /**
     * 한 창의 표본과 그 창을 잰 시각·레이트.
     *
     * 시각과 표본이 한 객체에 묶여 [AtomicReference] 로 건너가므로, 둘이 어긋난 짝이 만들어지지
     * 않는다.
     */
    private class AnalysisFrame(val samples: FloatArray) {
        var capturedAt = 0L
        var sampleRateHz = DefaultSampleRateHz
    }

    private data class TimedSpectrum(val measuredAt: Long, val spectrum: Spectrum)

    // 필드가 모두 자리를 잡은 뒤에 코루틴을 띄운다. 이 블록을 위쪽에 두면 분석 스레드가 아직
    // 초기화되지 않은 필드를 볼 여지가 생긴다.
    init {
        // 밴드별 상수의 길이가 밴드 수와 갈리면 분석 스레드에서 배열 밖을 읽는다. 컴파일러가
        // 잡아 주지 않는 어긋남이라 만드는 순간에 못 박는다.
        require(BandGainDecibels.size == Spectrum.BandCount && ReleaseSeconds.size == Spectrum.BandCount) {
            "밴드별 상수의 길이가 밴드 수와 어긋난다"
        }

        analysisScope.launch {
            // 아무도 보지 않는 동안에는 FFT 를 돌리지 않는다. 클립 탭을 벗어나면 구독이 끊기고
            // 루프도 멎는다. 다시 구독될 때는 그동안의 낡은 값을 모두 버리고 시작한다.
            _spectrum.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .collectLatest { active ->
                    if (active) {
                        analysisResetRequested = true
                        // 내부 상태만 비우면 마지막으로 내보낸 값이 그대로 남아, 돌아온 구독자가
                        // 지난 세션의 모양을 첫 값으로 받는다. 지연 큐까지 비우고 다시 시작하므로
                        // 새 값은 [PlaybackLatencyNanos] 뒤에야 나온다 — 그동안 옛 모양이 걸린다.
                        _spectrum.value = Spectrum.Silent
                        analysisLoop()
                    }
                }
        }
    }

    private companion object {
        // 실제 팟캐스트 클립을 재생하며 측정한 값으로 잡았다. 말소리는 대체로 -25~-14dBFS
        // 사이에 있고, 문장 사이의 정적이 -40 아래로 내려간다. 범위를 넓게 잡으면(처음에는
        // -48~-12 였다) level 이 0.5~0.9 에만 머물러 막대가 늘 비슷한 폭으로 흔들린다.
        /** 이 아래는 무음으로 본다. */
        const val SilenceDecibels = -40f

        /** 이 위로는 최대로 흔든다. */
        const val FullScaleDecibels = -14f

        /** log10(0) 을 피하기 위한 바닥. */
        const val MinimumRms = 1e-5f

        /**
         * 밴드별 감도 보정.
         *
         * 말소리는 저역에 힘이 몰려 있어 보정 없이 그리면 오른쪽 두 막대가 거의 눕는다. 반대로
         * 크게 올리면 다섯이 모두 천장에 붙어 통짜 사각형이 된다.
         *
         * 처음에는 장기평균 스펙트럼 모델로 계산해 0/0/1/4/8 을 넣었는데, 실제로 클립을
         * 100초 재생하며 [LogBandDecibels] 로 재보니 **한참 모자랐다.** 밴드별 상위 10%
         * 지점이 -21/-20/-22/-28/-35dB 에 머물러, 다섯 막대 모두 절반쯤 오르다 마는 데다
         * 시간의 40~58% 를 바닥에 붙어 있었다. 위 밴드일수록 더 눕는다는 방향만 맞았고
         * 필요한 양이 두 배 이상이었다 — 팟캐스트가 대개 낮은 비트레이트로 인코딩되어
         * 고역이 실제로 그만큼 얇다.
         *
         * 지금 값은 그 상위 10% 지점이 [FullScaleDecibels] 에 거의 닿도록 맞춘 것이다.
         * 그 결과 밴드별 상위 10% 가 0.90~0.96 까지 오르고 천장에 닿는 시간은 6~9% 에
         * 그친다. 다시 맞출 때도 같은 절차를 쓴다: [LogBandDecibels] 를 켜고 여러 클립을
         * 재생해 밴드별 원시 dB 를 모은 뒤, 상위 10% 지점이 [FullScaleDecibels] 근처에
         * 오도록 밴드마다 조정한다.
         */
        val BandGainDecibels = floatArrayOf(6f, 5f, 7f, 12f, 18f)

        /** 커지는 쪽 시간 상수. 밴드를 나누지 않는다 — 33ms 간격에서는 구분되지 않는다. */
        const val AttackSeconds = 0.025f

        /**
         * 놓는 속도. 너무 느리면 문장 사이의 정적이 반영되기 전에 다음 말이 시작된다.
         *
         * 위 밴드로 갈수록 길게 둔다. 치찰음은 30~80ms 짜리 섬광이라 아래와 같은 속도로 놓으면
         * 한 틱 반짝하고 사라져 눈에 남지 않는다.
         */
        val ReleaseSeconds = floatArrayOf(0.09f, 0.10f, 0.12f, 0.16f, 0.22f)

        /** 약 30Hz. 화면이 60fps 로 그려도 두 프레임에 한 번 새 값이면 충분하다. */
        const val PublishIntervalNanos = 33_000_000L

        /** 분석 루프의 주기. [PublishIntervalNanos] 와 같은 값을 밀리초로 본 것이다. */
        const val PublishIntervalMillis = 33L

        /**
         * 잰 시점과 실제로 들리는 시점의 차이.
         *
         * 밀어넣은 오디오의 길이에서 흐른 시간을 뺀 값(= AudioTrack 에 아직 쌓여 있는 분량)을
         * 재보니 0.39초에 안정적으로 수렴했다(에뮬레이터 Pixel 9 Pro, 변동 ±0.01). 기기와
         * 버퍼 설정에 따라 달라지는 값이라, 소리와 어긋나 보이면 여기부터 의심한다.
         */
        const val PlaybackLatencyNanos = 390_000_000L

        /** 지연 큐의 상한(약 1초분). 지연값이 어긋나도 메모리가 자라지 않게 한다. */
        const val MaxPendingSamples = 32

        /** 창 하나의 표본 수. 44.1kHz 에서 창 길이 23.2ms, 빈 폭 43Hz. */
        const val FftSize = 1024

        const val BytesPerSample = 2

        const val DefaultSampleRateHz = 44_100

        /** 스무딩에 쓰는 간격의 하한·상한. 시계가 튀어도 한 번에 뛰거나 멈추지 않게 한다. */
        const val MinDeltaNanos = 1_000_000L
        const val MaxDeltaNanos = 100_000_000L

        const val NanosPerSecond = 1e9f
        const val NanosPerSecondLong = 1_000_000_000L

        /**
         * 밴드 감도를 실제 소리로 맞출 때만 켜는 스위치.
         *
         * 켜면 1초에 한 번 밴드별 원시 dB 를 남긴다. 진행자 목소리·음악을 각각 재생해 그 분포를
         * 보고 [BandGainDecibels] 를 정한 뒤 다시 끈다.
         */
        const val LogBandDecibels = false

        const val LogIntervalNanos = 1_000_000_000L
    }
}

/**
 * 지금 시각(나노초)을 답한다.
 *
 * `() -> Long` 이 아니라 인터페이스인 것은, 그 타입이 `Function0<Long>` 으로 컴파일돼 부를
 * 때마다 `java.lang.Long` 을 하나씩 만들기 때문이다. 부르는 자리 하나가 오디오 스레드
 * 안이라 그 할당을 두지 않는다.
 */
internal fun interface NanoClock {
    fun nanos(): Long
}
