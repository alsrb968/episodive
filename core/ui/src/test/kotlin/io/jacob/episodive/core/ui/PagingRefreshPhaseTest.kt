package io.jacob.episodive.core.ui

import androidx.paging.LoadState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 이 판정이 틀리면 화면이 조용히 망가진다 — 결과가 0건인 목록에서 로딩 표시가 영원히 남거나,
 * 반대로 목록이 뜨기 직전에 "결과 없음"이 한 프레임 스쳐 지나간다.
 */
class PagingRefreshPhaseTest {

    @Test
    fun whenRefreshLoading_thenLoading() {
        assertEquals(
            PagingRefreshPhase.Loading,
            refreshPhaseOf(LoadState.Loading, itemCount = 0),
        )
    }

    @Test
    fun whenNotLoadingAndEndReachedWithNoItems_thenEmpty() {
        assertEquals(
            PagingRefreshPhase.Empty,
            refreshPhaseOf(LoadState.NotLoading(endOfPaginationReached = true), itemCount = 0),
        )
    }

    /** 로드를 시작하기 전 상태다. Empty 로 보내면 목록 직전에 "결과 없음"이 깜빡인다. */
    @Test
    fun whenNotLoadingBeforeFirstLoad_thenLoading() {
        assertEquals(
            PagingRefreshPhase.Loading,
            refreshPhaseOf(LoadState.NotLoading(endOfPaginationReached = false), itemCount = 0),
        )
    }

    @Test
    fun whenErrorWithNoItems_thenError() {
        assertEquals(
            PagingRefreshPhase.Error,
            refreshPhaseOf(LoadState.Error(RuntimeException("boom")), itemCount = 0),
        )
    }

    /** 이미 받아 둔 목록은 새로고침이 실패해도 지우지 않는다. */
    @Test
    fun whenErrorWithItems_thenContent() {
        assertEquals(
            PagingRefreshPhase.Content,
            refreshPhaseOf(LoadState.Error(RuntimeException("boom")), itemCount = 12),
        )
    }

    @Test
    fun whenItemsLoaded_thenContent() {
        assertEquals(
            PagingRefreshPhase.Content,
            refreshPhaseOf(LoadState.NotLoading(endOfPaginationReached = true), itemCount = 3),
        )
    }

    /** 첫 페이지를 받은 뒤 다음 페이지를 불러오는 중에도 목록은 그대로 보여야 한다. */
    @Test
    fun whenLoadingWithItems_thenContent() {
        assertEquals(
            PagingRefreshPhase.Content,
            refreshPhaseOf(LoadState.Loading, itemCount = 20),
        )
    }
}
