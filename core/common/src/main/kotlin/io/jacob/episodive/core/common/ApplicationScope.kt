package io.jacob.episodive.core.common

import javax.inject.Qualifier

/**
 * 화면보다 오래 살아야 하는 작업을 도는 코루틴 스코프.
 *
 * `viewModelScope` 나 `stateIn(WhileSubscribed(...))` 에 묶인 작업은 사용자가 화면을 떠나면
 * 얼마 뒤 취소된다. 화면에 보여줄 것과 수명이 같은 일에는 그것이 맞지만, **끝까지 마쳐야
 * 의미가 있는 일** — 캐시 갱신처럼 이번 화면이 아니라 다음 진입을 위한 일 — 은 중간에
 * 잘리면 아무것도 남기지 못한 채 비용만 쓴다.
 *
 * 특히 느린 원격이 그렇다. 응답이 5초를 넘기는 요청은 사용자가 탭을 옮기는 것만으로 매번
 * 취소되어 **영영 완주하지 못한다.** 그런 일을 여기에 태운다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
