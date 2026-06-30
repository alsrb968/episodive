package io.jacob.episodive.core.model

/**
 * 진행 중인 다운로드의 휘발성 상태. 진실의 원천은 시스템 DownloadManager이며 DB에 영속되지 않는다.
 * 완료 여부는 이 모델이 아니라 실제 파일 존재로 판정한다.
 */
data class DownloadProgress(
    val status: DownloadStatus,
    val downloadedBytes: Long,
    val totalBytes: Long,
) {
    // DownloadManager는 크기 확정 전 totalBytes를 -1로 보고한다.
    val isSizeKnown: Boolean = totalBytes > 0

    val progress: Float = if (isSizeKnown) {
        (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
    } else {
        0f
    }
}
