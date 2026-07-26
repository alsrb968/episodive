package io.jacob.episodive.core.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/** 페이징 목록의 첫 화면이 지금 무엇을 보여줘야 하는지. */
enum class PagingRefreshPhase {
    Loading,
    Empty,
    Error,
    Content,
}

/**
 * 첫 화면 상태를 [LoadState] 와 항목 수를 **함께** 보고 판정한다.
 *
 * 항목 수만 보고 로딩을 그리면 결과가 0건일 때 로딩 표시가 영원히 남는다 — 데이터가 끝내
 * 오지 않는 것과 애초에 없는 것을 구분할 수 없기 때문이다. 로드가 끝났는지(NotLoading)와
 * 실패했는지(Error)를 같이 봐야 안내 문구로 갈아탈 수 있다.
 *
 * 판정 규칙:
 * - 항목이 하나라도 있으면 [PagingRefreshPhase.Content]. 새로고침이 실패해도 이미 받아 둔
 *   목록은 지우지 않는다.
 * - 항목이 없고 실패했으면 [PagingRefreshPhase.Error].
 * - 항목이 없고 끝까지 읽었으면 [PagingRefreshPhase.Empty].
 * - 나머지는 [PagingRefreshPhase.Loading]. NotLoading 이면서 끝에 닿지 않은 상태는 아직
 *   로드를 시작하기 전이다. 이걸 Empty 로 보내면 목록이 뜨기 직전에 "결과 없음"이 한 프레임
 *   스쳐 지나간다.
 */
internal fun refreshPhaseOf(refresh: LoadState, itemCount: Int): PagingRefreshPhase = when {
    itemCount > 0 -> PagingRefreshPhase.Content
    refresh is LoadState.Error -> PagingRefreshPhase.Error
    refresh is LoadState.NotLoading && refresh.endOfPaginationReached -> PagingRefreshPhase.Empty
    else -> PagingRefreshPhase.Loading
}

/**
 * [refreshPhaseOf] 를 현재 페이징 상태에 적용한다.
 *
 * 컴포지션 안에서 호출해야 상태 변화가 관찰된다. 페이저가 [LazyListScope] 가 아닌 화면
 * (예: VerticalPager)에서 직접 분기할 때 쓴다.
 */
fun LazyPagingItems<*>.refreshPhase(): PagingRefreshPhase =
    refreshPhaseOf(loadState.refresh, itemCount)

/**
 * 첫 화면의 로딩·빈 목록·오류를 리스트 안에 방출한다.
 *
 * [PagingRefreshPhase.Content] 면 아무것도 방출하지 않으므로 실제 항목 블록 바로 앞에 두면
 * 된다. [key] 는 화면마다 다른 이름을 준다(예: `"podcast:episodes"`).
 */
fun LazyListScope.pagingRefreshState(
    items: LazyPagingItems<*>,
    key: String,
    loading: @Composable LazyItemScope.() -> Unit,
    empty: @Composable LazyItemScope.() -> Unit,
    error: @Composable LazyItemScope.() -> Unit = empty,
) {
    val refresh = items.loadState.refresh

    when (refreshPhaseOf(refresh, items.itemCount)) {
        PagingRefreshPhase.Content -> Unit

        PagingRefreshPhase.Loading -> item(
            key = pagingStateKey(key, "refresh"),
            contentType = PagingStateContentType,
            content = loading,
        )

        PagingRefreshPhase.Empty -> item(
            key = pagingStateKey(key, "empty"),
            contentType = PagingStateContentType,
            content = empty,
        )

        PagingRefreshPhase.Error -> item(
            key = pagingStateKey(key, "error"),
            contentType = PagingStateContentType,
            content = error,
        )
    }
}

/**
 * 목록 아래쪽 추가 로딩을 방출한다. 실제 항목 블록 **뒤**에 둔다.
 *
 * 하단 여백용 Spacer 가 있는 리스트라면 그 Spacer 보다 앞에 와야 여백 위에 붙는다.
 */
fun LazyListScope.pagingAppendState(
    items: LazyPagingItems<*>,
    key: String,
    loading: @Composable LazyItemScope.() -> Unit,
    error: (@Composable LazyItemScope.() -> Unit)? = null,
) {
    when (items.loadState.append) {
        is LoadState.Loading -> item(
            key = pagingStateKey(key, "append"),
            contentType = PagingStateContentType,
            content = loading,
        )

        is LoadState.Error -> if (error != null) {
            item(
                key = pagingStateKey(key, "appendError"),
                contentType = PagingStateContentType,
                content = error,
            )
        }

        is LoadState.NotLoading -> Unit
    }
}

/**
 * 상태 아이템의 키는 실제 항목과 네임스페이스를 나눈다.
 *
 * 항목 키로 문자열을 쓰는 목록(보관함의 날짜 구분선 등)과 우연히 겹치면 Lazy 레이아웃이
 * 서로를 같은 항목으로 본다. contentType 도 함께 달리해야 슬롯 재사용이 상태 아이템을
 * 실제 카드 자리에 끼워 넣지 않는다.
 */
private fun pagingStateKey(key: String, suffix: String) = "pagingState:$key:$suffix"

private const val PagingStateContentType = "pagingState"
