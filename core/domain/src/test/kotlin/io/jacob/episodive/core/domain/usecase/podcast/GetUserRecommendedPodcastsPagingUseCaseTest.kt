package io.jacob.episodive.core.domain.usecase.podcast

import androidx.paging.LoadState
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import app.cash.turbine.test
import io.jacob.episodive.core.domain.repository.PodcastRepository
import io.jacob.episodive.core.domain.repository.UserRepository
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.UserData
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GetUserRecommendedPodcastsPagingUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val podcastRepository = mockk<PodcastRepository>(relaxed = true)

    private val useCase = GetUserRecommendedPodcastsPagingUseCase(
        userRepository = userRepository,
        podcastRepository = podcastRepository,
    )

    @After
    fun teardown() {
        confirmVerified(
            userRepository,
            podcastRepository,
        )
    }

    @Test
    fun `Given dependencies, when invoke called, then repository called`() =
        runTest {
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(
                UserData(
                    language = "ko",
                    categories = listOf(Category.AFTER_SHOWS, Category.BUSINESS)
                )
            )
            coEvery {
                podcastRepository.getRecommendedPodcastsPaging(any(), any(), any())
            } returns mockk(relaxed = true)

            // When
            useCase(50).test {
                awaitComplete()
            }

            // Then
            coVerifySequence {
                userRepository.getUserData()
                podcastRepository.getRecommendedPodcastsPaging(
                    50,
                    "ko",
                    listOf(Category.AFTER_SHOWS, Category.BUSINESS)
                )
            }
        }

    @Test
    fun `Given empty categories, when invoke called, then paging ends instead of loading forever`() =
        runTest {
            // 온보딩의 '다음' 버튼은 선택 개수를 막지 않으므로(enabled = true) 카테고리를
            // 하나도 안 고른 채 팟캐스트 선택 화면에 닿을 수 있다. 이때 무인자
            // PagingData.empty() 를 흘리면 로드 상태가 Loading 에 머물러 스켈레톤이 영원히
            // 반짝인다 — 화면이 '결과 없음'으로 판정하려면 끝에 닿았음을 알려야 한다.
            // Given
            coEvery {
                userRepository.getUserData()
            } returns flowOf(UserData(language = "ko", categories = emptyList()))

            // When
            val presenter = object : PagingDataPresenter<Podcast>() {
                override suspend fun presentPagingDataEvent(event: PagingDataEvent<Podcast>) = Unit
            }
            val collecting = launch { presenter.collectFrom(useCase(50).first()) }
            advanceUntilIdle()

            // Then
            assertEquals(
                LoadState.NotLoading(endOfPaginationReached = true),
                presenter.loadStateFlow.value?.refresh,
            )
            collecting.cancel()

            coVerify { userRepository.getUserData() }
        }
}