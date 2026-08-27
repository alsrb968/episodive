package io.jacob.episodive.core.model.opml

/**
 * OPML 들여오기 진행 상황. 항목을 하나씩 처리할 때마다 새 값을 방출해 화면이
 * 진행률을 실시간으로 그릴 수 있게 한다.
 */
data class OpmlImportProgress(
    /** 파일에서 읽어낸 outline 총 개수. */
    val total: Int,
    /** 지금까지 처리(성공/실패 무관)를 마친 개수. */
    val done: Int = 0,
    /** 새로 팔로우에 추가된 개수. */
    val added: Int = 0,
    /** 이미 팔로우 중이라 건너뛴 개수. */
    val alreadyFollowed: Int = 0,
    /**
     * Podcast Index 에서 그 피드를 찾지 못한 개수. 존재하지 않는 피드를 조회하면
     * 예외가 아니라 빈 결과가 조용히 온다 — 사용자가 재시도해도 해결되지 않으므로
     * 네트워크 실패([failed])와는 화면에서 다르게 안내해야 한다.
     */
    val notFound: Int = 0,
    /**
     * 네트워크 오류 등으로 처리에 실패한 항목의 제목 목록. [notFound] 와 달리
     * 재시도하면 성공할 여지가 있다.
     */
    val failed: List<String> = emptyList(),
    /**
     * 완료 여부. 완료 통지를 일회성 Effect 로 보내면 사용자가 다른 탭으로 이동한
     * 사이 끝났을 때 증발한다(이 저장소의 `_effect` 는 replay 0 SharedFlow 다).
     * 그래서 완료 사실을 상태 자체에 남겨 화면이 다시 구독해도 알 수 있게 한다.
     */
    val isFinished: Boolean = false,
    /**
     * 오프라인이 감지돼 루프를 중간에 끊었는지 여부. 오프라인이면 남은 항목도
     * 전부 같은 이유로 실패할 것이 뻔하므로 하나씩 실패시키지 않고 그 자리에서
     * 멈춘다. 화면은 이 값으로 "실패 N건" 대신 "네트워크 확인" 같은 안내를 보여준다.
     */
    val stoppedOffline: Boolean = false,
) {
    /**
     * 0..1 진행률. total 이 0 이면 아직 알 수 없으므로 0으로 취급한다.
     *
     * 범위를 여기서 조인다. 진행률 표시기에 1을 넘는 값이 가면 그리는 쪽이 이상해지는데,
     * 그 방어를 화면에 맡기면 계산이 두 곳으로 갈라진다.
     */
    val progress: Float
        get() = if (total == 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
}
