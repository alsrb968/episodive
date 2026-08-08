package io.jacob.episodive.core.domain.util

import androidx.paging.LoadState
import androidx.paging.LoadStates

/**
 * "더 불러올 것이 없는 빈 목록"의 로드 상태.
 *
 * `PagingData.empty()` 의 무인자 오버로드는 로드 상태를 **갱신하지 않는다**. Paging 의 초기
 * 상태는 `refresh = Loading` 이므로, 그대로 두면 항목이 0개인데도 화면은 계속 로딩으로 읽는다
 * — 사용자에게는 스켈레톤이 영원히 반짝이는 것으로 보인다.
 *
 * 끝에 닿았음을 명시해야 `refreshPhaseOf` 가 Empty 로 판정하고 안내 문구가 나온다.
 */
val EmptyLoadStates = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)

/**
 * 원격 실패를 Paging 의 오류 상태로 옮긴 빈 목록.
 *
 * 캐시가 아예 없는 첫 진입에서 원격이 실패하면 `RemoteUpdater` 가 예외를 흐름 밖으로 던진다
 * (`cachedAt == null` 분기). 그 예외를 잡지 않으면 `cachedIn` 이 공유하는 코루틴에서 그대로
 * 터져 화면 오류가 아니라 앱이 죽는다. 여기로 옮겨 두면 화면이 이미 갖춘 오류·재시도 경로가
 * 그대로 받는다.
 */
fun errorLoadStates(error: Throwable) = LoadStates(
    refresh = LoadState.Error(error),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)
