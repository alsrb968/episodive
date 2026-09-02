package io.jacob.episodive.core.player.audio

import io.jacob.episodive.core.model.Spectrum
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 모노 표본 한 창을 받아 주파수 대역 다섯 칸의 RMS 를 낸다.
 *
 * 라이브러리를 쓰지 않고 직접 쓴 radix-2 Cooley-Tukey 다. 필요한 것이 고정 크기 실수 FFT
 * 하나뿐이라 의존성을 하나 더 늘릴 이유가 없었다.
 *
 * **스레드 안전하지 않다.** 작업 배열을 인스턴스가 계속 재사용하므로(호출당 할당을 0 으로
 * 두기 위한 선택이다) 두 스레드가 동시에 [bandRms] 를 부르면 서로의 중간값을 밟는다. 분석
 * 코루틴 하나만 이 객체를 만진다는 전제로 쓴다.
 *
 * 실수 입력을 절반 크기 복소 FFT 로 접는 최적화(실수 패킹)는 하지 않았다. 연산이 절반으로
 * 주는 대신 부호와 대칭을 손으로 맞춰야 하는 자리가 두 배로 늘어나는데, 33ms 에 한 번 도는
 * 1024점 FFT 에는 그만한 값이 없다.
 */
internal class FftSpectrumAnalyzer(private val fftSize: Int = DefaultFftSize) {
    /** 단측 스펙트럼에서 의미가 있는 마지막 빈. */
    private val maxBin = fftSize / 2 - 1

    /**
     * Hann 창. 창 없이(= 사각창) 자르면 창 경계의 불연속이 사이드로브로 퍼져 강한 저역 하나가
     * 위쪽 밴드까지 들어 올린다. 밴드를 나누는 것이 목적인 이상 창은 선택이 아니다.
     */
    private val hann = FloatArray(fftSize) {
        0.5f * (1.0 - cos(2.0 * PI * it / (fftSize - 1))).toFloat()
    }

    private val re = FloatArray(fftSize)
    private val im = FloatArray(fftSize)

    /** 비트리버설 순열. 매 호출 비트를 뒤집는 것보다 4KB 를 들고 있는 편이 싸다. */
    private val bitReversed = IntArray(fftSize) { index ->
        var reversed = 0
        var value = index
        var bit = fftSize shr 1
        while (bit > 0) {
            if (value and 1 == 1) reversed = reversed or bit
            value = value shr 1
            bit = bit shr 1
        }
        reversed
    }

    // 트위들 계수 exp(-2πi*j/N). 모든 단계가 이 표를 간격만 달리해 훑는다.
    private val cosTable = FloatArray(fftSize / 2) { cos(-2.0 * PI * it / fftSize).toFloat() }
    private val sinTable = FloatArray(fftSize / 2) { sin(-2.0 * PI * it / fftSize).toFloat() }

    /** 밴드별 빈 범위. 샘플레이트가 바뀔 때만 다시 짓는다. */
    private var bandBins: List<IntRange> = bandBinsFor(DefaultSampleRateHz)
    private var mappedSampleRateHz = DefaultSampleRateHz

    /**
     * [samples] 한 창(길이 [fftSize])을 분석해 밴드별 RMS 를 [out](길이 [BandCount])에 채운다.
     *
     * **호출당 할당이 없다.** 오디오와 같은 주기로 도는 경로라 작업 배열은 모두 생성자에서
     * 잡아 두고 여기서는 덮어쓰기만 한다. 람다·컬렉션 연산도 같은 이유로 쓰지 않는다.
     */
    fun bandRms(samples: FloatArray, sampleRateHz: Int, out: FloatArray) {
        // 밴드 수가 어긋나면 여기서 out 배열 밖을 쓴다. 그 예외는 분석 코루틴에서 터져
        // 막대만 조용히 멎으므로, 원인이 드러나는 자리에서 먼저 막는다.
        require(out.size == BandCount) { "밴드 수가 맞지 않는다: ${out.size} != $BandCount" }

        if (sampleRateHz != mappedSampleRateHz) {
            bandBins = bandBinsFor(sampleRateHz)
            mappedSampleRateHz = sampleRateHz
        }

        // 창을 씌우면서 곧바로 비트리버설 자리에 놓는다. 순열과 창 적용을 한 번에 끝낸다.
        for (index in 0 until fftSize) {
            val source = bitReversed[index]
            re[index] = samples[source] * hann[source]
            im[index] = 0f
        }

        transform()

        // amp[k] = 2*|X[k]| / (N * sqrt(3/8)).
        // 2/N 은 단측 스펙트럼 환산이고, sqrt(3/8) 은 Hann 창의 진폭 손실(창의 제곱평균)을
        // 되돌린다. 이것을 빼먹으면 진폭이 sqrt(3/8) 배로 남아 **모든 밴드가 한결같이
        // 4.26dB 낮아지고**, 무음 판정선이 그만큼 어긋난 채로 조용히 동작한다. (에너지합
        // 방식에는 이 계수가 맞다. 코히런트 게인 0.5 를 대신 쓰면 반대로 1.76dB 들뜬다.)
        val scale = 2f / (fftSize * sqrt(3f / 8f))
        val powerScale = scale * scale

        for (band in 0 until BandCount) {
            val bins = bandBins[band]
            var energy = 0.0
            var bin = bins.first
            val last = bins.last
            while (bin <= last) {
                val real = re[bin]
                val imaginary = im[bin]
                // |X[k]|^2 을 그대로 더한다. 빈마다 sqrt 를 부르고 다시 제곱할 이유가 없다.
                energy += (real * real + imaginary * imaginary).toDouble()
                bin++
            }
            // 진폭을 RMS 로 바꾸는 1/sqrt(2) 를 에너지 쪽에서 2 로 나눠 처리한다.
            out[band] = sqrt(energy * powerScale / 2.0).toFloat()
        }
    }

    /**
     * 이 샘플레이트에서 각 밴드가 차지하는 빈 범위. 테스트가 경계를 직접 단언할 수 있게
     * 열어 둔다 — 톤 하나를 넣어 보는 검사는 경계가 한두 빈 밀려도 초록으로 지나간다.
     *
     * 여기는 [bandRms] 와 달리 호출당 할당이 있다. 샘플레이트가 바뀌는 순간과 테스트에서만
     * 불린다.
     */
    internal fun bandBinsFor(sampleRateHz: Int): List<IntRange> {
        val binHz = sampleRateHz.toFloat() / fftSize
        return List(BandCount) { band ->
            // 반개구간이라 밴드끼리 겹치지 않는다: 빈 k 는 edges[i] <= k*binHz < edges[i+1] 인
            // 밴드에 속한다. 아래끝을 1 로 막는 것은 DC(빈 0)를 어떤 밴드에도 넣지 않기 위해서다.
            val start = max(1, ceil(BandEdgesHz[band] / binHz).toInt())
            val end = min(maxBin, ceil(BandEdgesHz[band + 1] / binHz).toInt() - 1)
            // Nyquist 위로 밀려난 밴드는 빈 범위가 되어 늘 0 을 낸다.
            if (start > end) IntRange.EMPTY else start..end
        }
    }

    /** 제자리 radix-2 버터플라이. 입력은 이미 비트리버설 순서로 놓여 있어야 한다. */
    private fun transform() {
        var length = 2
        while (length <= fftSize) {
            val half = length shr 1
            val step = fftSize / length
            var base = 0
            while (base < fftSize) {
                for (pair in 0 until half) {
                    val twiddle = pair * step
                    val wr = cosTable[twiddle]
                    val wi = sinTable[twiddle]
                    val top = base + pair
                    val bottom = top + half
                    val tr = re[bottom] * wr - im[bottom] * wi
                    val ti = re[bottom] * wi + im[bottom] * wr
                    re[bottom] = re[top] - tr
                    im[bottom] = im[top] - ti
                    re[top] += tr
                    im[top] += ti
                }
                base += length
            }
            length = length shl 1
        }
    }

    init {
        // 경계 표와 화면이 아는 밴드 수가 갈리면 조용히 최상위 밴드가 사라지거나 배열 밖을
        // 쓴다. 어느 쪽도 컴파일러가 잡아 주지 않으므로 만드는 순간에 못 박는다.
        require(BandCount == Spectrum.BandCount) {
            "밴드 경계와 Spectrum.BandCount 가 어긋난다: $BandCount != ${Spectrum.BandCount}"
        }
    }

    internal companion object {
        /**
         * 밴드 경계(Hz). 낮은 쪽부터 남성 F0·저역(80–250), 말소리의 몸통(250–500), F1/F2
         * (500–1200), 자음 명료도(1200–3000), 치찰음·공기감(3000–8000)이다.
         *
         * 아래를 80Hz 에서 끊는 것은 그 밑이 럼블과 DC 누설이고 팟캐스트 대부분이 이미
         * 하이패스해 늘 죽은 막대가 되기 때문이다. 위를 8kHz 에서 끊는 것도 같은 이유로,
         * 64~128kbps 인코딩이 그 위를 잘라 버린다.
         */
        val BandEdgesHz = floatArrayOf(80f, 250f, 500f, 1200f, 3000f, 8000f)

        /**
         * 밴드 수는 [BandEdgesHz] 에서 유도한다. 따로 적어 두면 경계만 하나 늘렸을 때
         * 최상위 밴드가 소리 없이 사라지고, 반대로 이 숫자만 늘리면 경계 표 밖을 읽는다.
         */
        val BandCount = BandEdgesHz.size - 1

        /**
         * 44.1kHz 에서 빈 폭 43Hz, 창 길이 23.2ms. 더 키우면(2048) 최저 밴드의 폭이 Hann
         * 주엽 폭과 비슷해져 아래 두 밴드가 원리상 갈리지 않는다.
         */
        const val DefaultFftSize = 1024

        private const val DefaultSampleRateHz = 44_100
    }
}
