package io.jacob.episodive.core.player.audio

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * 재생 중인 소리의 크기를 0..1 로 내보낸다.
 *
 * media3 의 오디오 프로세서 체인에 [TeeAudioProcessor] 로 끼워, 디코딩이 끝나고 `AudioTrack`
 * 으로 넘어가기 직전의 PCM 을 들여다본다. 자기 앱이 디코딩한 자기 오디오라 **`RECORD_AUDIO`
 * 권한이 필요 없다.** (`android.media.audiofx.Visualizer` 는 같은 일을 하면서 마이크 권한을
 * 요구한다 — 막대 다섯 개를 위해 치를 값이 아니다.)
 *
 * 세 가지를 조심한다.
 *
 * 1. [handleBuffer] 는 **오디오 스레드에서** 불린다. 여기서 시간을 끌면 버퍼가 비어 소리가
 *    끊긴다. 그래서 하는 일은 제곱합 하나뿐이고, FFT 같은 것은 여기 두지 않는다.
 * 2. **여기서 보는 것은 지금 들리는 소리가 아니라 곧 들릴 소리다.** 그대로 내보내면 막대가
 *    소리보다 먼저 움직인다. [PlaybackLatencyNanos] 만큼 늦춰 내보내는 이유다.
 * 3. 오디오 오프로드를 켜면 압축 데이터가 DSP 로 직행해 이 경로로 아무것도 오지 않는다.
 *    크래시가 아니라 "막대가 안 움직임" 으로 나타나니, 오프로드를 켜게 되면 이 사실을
 *    함께 적어야 한다.
 */
@Singleton
class PlaybackAmplitudeMonitor @Inject constructor() : TeeAudioProcessor.AudioBufferSink {
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    /** [flush] 는 오디오 스레드 밖에서도 불릴 수 있어 [handleBuffer] 와 스레드가 갈린다. */
    @Volatile
    private var pcmEncoding: Int = C.ENCODING_PCM_16BIT

    /** 다른 스레드의 [reset] 을 오디오 스레드가 받아 가는 통로. */
    @Volatile
    private var resetRequested = false

    private var smoothedLevel = 0f
    private var lastPublishedAt = 0L

    /** (잰 시각, 그때의 크기). 오디오 스레드에서만 만지므로 별도 동기화가 없다. */
    private val pending = ArrayDeque<TimedLevel>()

    @OptIn(UnstableApi::class)
    override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
        pcmEncoding = encoding
        reset()
    }

    override fun handleBuffer(buffer: ByteBuffer) {
        // 16bit PCM 만 읽는다. float 출력은 기본으로 꺼져 있고, 켜졌다면 조용히 0 을 유지한다.
        if (pcmEncoding != C.ENCODING_PCM_16BIT) return

        // 큐를 다른 스레드에서 비우면 오디오 스레드가 그 자리에서 락을 기다리게 된다. 비우라는
        // 표시만 받아 두고, 실제로 비우는 것은 여기 오디오 스레드에서 한다.
        if (resetRequested) {
            resetRequested = false
            pending.clear()
            smoothedLevel = 0f
            lastPublishedAt = 0L
        }

        val rms = buffer.rootMeanSquare() ?: return

        // 사람 귀는 소리 크기를 로그로 듣는다. 선형 RMS 를 그대로 높이에 쓰면 말소리 대부분이
        // 아래쪽에 눌려붙어 거의 움직이지 않는다.
        val decibels = 20f * log10(rms.coerceAtLeast(MinimumRms))
        val level = ((decibels - SilenceDecibels) / (FullScaleDecibels - SilenceDecibels))
            .coerceIn(0f, 1f)

        // 커질 때는 빠르게 따라가고 잦아들 때는 천천히 놓는다. 둘을 같은 속도로 두면 자음마다
        // 막대가 바닥까지 떨어져 깜빡이는 것처럼 보인다.
        val ratio = if (level > smoothedLevel) AttackRatio else ReleaseRatio
        smoothedLevel += (level - smoothedLevel) * ratio

        // 방금 잰 것은 AudioTrack 에 쌓였다가 잠시 뒤에 들릴 소리다. 잰 시각과 함께 넣어 두고
        // 그만큼 지난 것만 꺼내 내보낸다.
        val now = System.nanoTime()
        pending.addLast(TimedLevel(measuredAt = now, level = smoothedLevel))

        val audibleAt = now - PlaybackLatencyNanos
        var audibleLevel: Float? = null
        while (pending.isNotEmpty() && pending.first().measuredAt <= audibleAt) {
            audibleLevel = pending.removeFirst().level
        }

        // 실제 지연이 예상보다 큰 기기에서 큐가 끝없이 자라지 않게 막는다.
        while (pending.size > MaxPendingSamples) pending.removeFirst()

        // 버퍼는 초당 수십~수백 번 들어온다. 그대로 흘리면 화면이 따라갈 수 없는 빈도로
        // 코루틴이 깨어난다. 화면 갱신에 필요한 만큼만 내보낸다.
        if (audibleLevel == null || now - lastPublishedAt < PublishIntervalNanos) return
        lastPublishedAt = now
        _amplitude.value = audibleLevel
    }

    /** 재생이 멎으면 마지막 크기가 그대로 남으므로 직접 되돌린다. */
    fun reset() {
        resetRequested = true
        _amplitude.value = 0f
    }

    /**
     * 버퍼의 제곱평균제곱근(0..1). 읽을 표본이 없으면 null.
     *
     * `position` 을 건드리지 않도록 절대 인덱스로 읽는다. 넘어오는 버퍼는 읽기 전용 뷰지만,
     * 이 sink 가 커서를 옮겨도 원본이 무사하다는 보장에 기대지 않는 편이 안전하다.
     */
    private fun ByteBuffer.rootMeanSquare(): Float? {
        var squareSum = 0.0
        var sampleCount = 0
        var index = position()
        val end = limit()

        while (index + 1 < end) {
            // 16bit PCM 은 리틀엔디안이다. 버퍼의 바이트 순서 설정에 기대지 않고 직접 맞춘다.
            val sample = (get(index + 1).toInt() shl 8) or (get(index).toInt() and 0xFF)
            val normalized = sample / Short.MAX_VALUE.toFloat()
            squareSum += (normalized * normalized).toDouble()
            sampleCount++
            index += SampleStrideBytes
        }

        if (sampleCount == 0) return null
        return sqrt(squareSum / sampleCount).toFloat()
    }

    private data class TimedLevel(val measuredAt: Long, val level: Float)

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

        const val AttackRatio = 0.6f

        /** 놓는 속도. 너무 느리면 문장 사이의 정적이 반영되기 전에 다음 말이 시작된다. */
        const val ReleaseRatio = 0.22f

        /** 약 30Hz. 화면이 60fps 로 그려도 두 프레임에 한 번 새 값이면 충분하다. */
        const val PublishIntervalNanos = 33_000_000L

        /**
         * 잰 시점과 실제로 들리는 시점의 차이.
         *
         * 밀어넣은 오디오의 길이에서 흐른 시간을 뺀 값(= AudioTrack 에 아직 쌓여 있는 분량)을
         * 재보니 0.39초에 안정적으로 수렴했다(에뮬레이터 Pixel 9 Pro, 변동 ±0.01). 기기와
         * 버퍼 설정에 따라 달라지는 값이라, 소리와 어긋나 보이면 여기부터 의심한다.
         */
        const val PlaybackLatencyNanos = 390_000_000L

        /** 지연 큐의 상한(약 2초분). 지연값이 어긋나도 메모리가 자라지 않게 한다. */
        const val MaxPendingSamples = 64

        /** 표본을 네 개마다 하나씩(16bit 이므로 8바이트) 본다. 크기 추정에는 넉넉하다. */
        const val SampleStrideBytes = 8
    }
}
