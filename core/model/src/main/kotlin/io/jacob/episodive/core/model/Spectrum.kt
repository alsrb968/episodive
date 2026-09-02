package io.jacob.episodive.core.model

/**
 * 지금 나고 있는 소리를 주파수 대역 다섯 칸으로 나눈 세기.
 *
 * 세 가지를 알고 써야 한다.
 *
 * 1. [levels] 는 **낮은 주파수부터** 늘어선다. 0번이 저역, 4번이 치찰음 쪽이다.
 * 2. 각 값은 0..1 이지만 물리적 세기가 아니라 **그 밴드의 dB 창 안에서의 상대 위치**다.
 *    밴드마다 창의 기준이 다르므로 **두 막대의 높이가 같다고 두 대역의 에너지가 같은 것이
 *    아니다.** 이 값은 눈으로 보는 용도이지 재는 용도가 아니다.
 * 3. 실제로 귀에 닿는 소리보다 AudioTrack 버퍼만큼 앞선 값이다. 내보내는 쪽이 그만큼 늦춰
 *    주지만, 기기마다 다른 지연을 하나의 상수로 맞춘 것이라 완전히 겹치지는 않는다.
 *
 * `FloatArray` 가 아니라 `List<Float>` 인 이유: 배열은 참조 동일성으로만 같다고 보므로 무음이
 * 이어지는 동안에도 매번 새 값으로 취급돼 StateFlow 가 화면을 계속 깨운다. 리스트는 구조적
 * 동등성이라 같은 값이 이어지면 조용히 접힌다.
 */
@JvmInline
value class Spectrum(val levels: List<Float>) {
    /** 밴드 수가 막대 수와 어긋나도 화면이 깨지지 않도록, 없는 자리는 잠잠한 것으로 답한다. */
    fun level(band: Int): Float = levels.getOrElse(band) { 0f }

    companion object {
        const val BandCount = 5

        /** 소리가 없거나 분석이 붙지 않은 플레이어의 값. */
        val Silent = Spectrum(List(BandCount) { 0f })
    }
}
