package io.jacob.episodive.core.model

import io.jacob.episodive.core.model.mapper.toIntSeconds
import kotlin.time.Duration

data class Progress(
    val position: Duration,
    val buffered: Duration,
    val duration: Duration,
    /**
     * 이 진행률이 어느 에피소드의 것인지. 재생 위치를 DB 에 저장할 때 쓰는 유일한 키다.
     *
     * 위치와 에피소드를 한 값에 묶는 이유: 둘을 별도 Flow 로 두면 에피소드 전환 순간
     * 지연이 다른 두 스트림이 어긋나 "이전 에피소드 + 새 위치" 쌍이 만들어지고,
     * 그대로 저장되어 이전 에피소드의 이어듣기 지점이 오염된다.
     *
     * null 은 "재생 대상 미확정"(프로세스 시작 직후, rehydrate 직후)을 뜻하며 저장하지 않는다.
     */
    val episodeId: Long? = null,
) {
    val positionRatio: Float =
        if (duration.isPositive() && duration.toIntSeconds() > 0) {
            (position.toIntSeconds().toFloat() / duration.toIntSeconds()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val bufferedRatio: Float =
        if (duration.isPositive() && duration.toIntSeconds() > 0) {
            (buffered.toIntSeconds().toFloat() / duration.toIntSeconds()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val remaining: Duration = (duration - position).coerceAtLeast(Duration.ZERO)
    val remainingRatio: Float =
        if (duration.isPositive() && duration.toIntSeconds() > 0) {
            (remaining.toIntSeconds().toFloat() / duration.toIntSeconds()).coerceIn(0f, 1f)
        } else {
            0f
        }
}