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

/**
 * [LocalNowPlayingEpisodeId] 의 그 에피소드가 지금 **소리를 내고 있는지**.
 *
 * 위 id 는 플레이어에 올라와 있기만 해도 값이 남아 일시정지를 구분하지 못한다. 목록 항목의
 * 강조(진행률 링)는 일시정지 중에도 유지되는 편이 자연스러워 그 의미를 바꾸지 않고, 재생과
 * 일시정지를 갈라야 하는 곳만 이 값을 함께 본다.
 *
 * media3 의 `isPlaying` 을 그대로 싣는다. 버퍼링 중이거나 오디오 포커스를 잠깐 빼앗긴 동안에도
 * false 라, 탭 직후 잠시 false 로 머무를 수 있다.
 */
val LocalIsPlaying = compositionLocalOf { false }
