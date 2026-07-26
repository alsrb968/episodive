package io.jacob.episodive.core.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * 지금 재생 중인 에피소드 id.
 *
 * 목록 컴포넌트가 "이 항목이 재생 중인가"를 스스로 판단할 수 있게 앱 최상위에서 내려준다.
 * 각 화면의 상태(State)에 재생 정보를 끼워 넣지 않고 UI 계층에서만 전달하기 위한 통로다.
 * 재생 중인 것이 없으면 null.
 */
val LocalNowPlayingEpisodeId = compositionLocalOf<Long?> { null }
