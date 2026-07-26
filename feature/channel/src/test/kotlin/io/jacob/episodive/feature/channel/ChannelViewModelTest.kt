package io.jacob.episodive.feature.channel

import app.cash.turbine.test
import io.jacob.episodive.core.domain.usecase.channel.GetChannelByIdUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastsByChannelUseCase
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.DataErrorException
import io.jacob.episodive.core.testing.model.channelTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChannelViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getChannelByIdUseCase = mockk<GetChannelByIdUseCase>(relaxed = true)
    private val getPodcastsByChannelUseCase = mockk<GetPodcastsByChannelUseCase>(relaxed = true)

    private fun createViewModel(id: Long = 1L): ChannelViewModel {
        return ChannelViewModel(
            getChannelByIdUseCase = getChannelByIdUseCase,
            getPodcastsByChannelUseCase = getPodcastsByChannelUseCase,
            id = id,
        )
    }

    @Test
    fun `Given no emissions, When ViewModel is created, Then initial state is Loading`() = runTest {
        every { getChannelByIdUseCase(any()) } returns flowOf()

        val viewModel = createViewModel()

        assertEquals(ChannelState.Loading, viewModel.state.value)
    }

    @Test
    fun `Given valid channel and podcasts, When flows emit, Then state is Success`() = runTest {
        val channel = channelTestData
        val podcasts = podcastTestDataList.take(3)
        every { getChannelByIdUseCase(1L) } returns flowOf(channel)
        every { getPodcastsByChannelUseCase(channel) } returns flowOf(podcasts)

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is ChannelState.Success)
            val success = state as ChannelState.Success
            assertEquals(channel, success.channel)
            assertEquals(podcasts, success.podcasts)
        }
    }

    @Test
    fun `Given null channel, When flow emits, Then state is Error with NotFound`() = runTest {
        every { getChannelByIdUseCase(1L) } returns flowOf(null)

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is ChannelState.Error)
            assertEquals(DataError.NotFound, (state as ChannelState.Error).error)
        }
    }

    @Test
    fun `Given flow throws unrecognized exception, When collecting, Then state is Error with Unexpected`() =
        runTest {
            val exception = RuntimeException("Network error")
            every { getChannelByIdUseCase(1L) } returns kotlinx.coroutines.flow.flow {
                throw exception
            }

            val viewModel = createViewModel()

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ChannelState.Error)
                val error = (state as ChannelState.Error).error
                assertTrue(error is DataError.Unexpected)
                // coroutine 경계를 넘을 때 kotlinx.coroutines 가 스택트레이스 복구를 위해
                // 예외를 복제할 수 있어 참조(exception) 동일성이 아니라 내용으로 비교한다.
                assertEquals(exception.message, (error as DataError.Unexpected).throwable?.message)
            }
        }

    @Test
    fun `Given flow throws DataErrorException, When collecting, Then state is Error with the mapped DataError`() =
        runTest {
            every { getChannelByIdUseCase(1L) } returns kotlinx.coroutines.flow.flow {
                throw DataErrorException(DataError.Offline)
            }

            val viewModel = createViewModel()

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ChannelState.Error)
                assertEquals(DataError.Offline, (state as ChannelState.Error).error)
            }
        }

    @Test
    fun `Given valid channel with empty podcasts, When flows emit, Then state is Success with empty list`() =
        runTest {
            val channel = channelTestData
            every { getChannelByIdUseCase(1L) } returns flowOf(channel)
            every { getPodcastsByChannelUseCase(channel) } returns flowOf(emptyList())

            val viewModel = createViewModel()

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ChannelState.Success)
                val success = state as ChannelState.Success
                assertEquals(channel, success.channel)
                assertTrue(success.podcasts.isEmpty())
            }
        }

    @Test
    fun `Given ClickBack action, When sent, Then NavigateBack effect is emitted`() = runTest {
        every { getChannelByIdUseCase(1L) } returns flowOf(channelTestData)
        every { getPodcastsByChannelUseCase(any()) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.sendAction(ChannelAction.ClickBack)
            assertEquals(ChannelEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `Given ClickPodcast action, When sent, Then NavigateToPodcast effect is emitted`() =
        runTest {
            every { getChannelByIdUseCase(1L) } returns flowOf(channelTestData)
            every { getPodcastsByChannelUseCase(any()) } returns flowOf(emptyList())

            val viewModel = createViewModel()

            viewModel.effect.test {
                viewModel.sendAction(ChannelAction.ClickPodcast(42L))
                assertEquals(ChannelEffect.NavigateToPodcast(42L), awaitItem())
            }
        }

    @Test
    fun `Given Retry action, When sent, Then upstream flow chain is resubscribed`() = runTest {
        every { getChannelByIdUseCase(1L) } returns flowOf(channelTestData)
        every { getPodcastsByChannelUseCase(any()) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.state.test {
            assertTrue(awaitItem() is ChannelState.Success)

            viewModel.sendAction(ChannelAction.Retry)
            advanceUntilIdle()

            // retryTrigger 가 바뀌면 flatMapLatest 가 상위 체인(getChannelByIdUseCase 호출부터)을
            // 통째로 재구독한다. Success 값 자체는 이전과 구조적으로 같아 StateFlow가 재방출을
            // 걸러내므로, 두 번째 awaitItem() 대신 UseCase 호출 횟수로 재구독을 검증한다.
            verify(exactly = 2) { getChannelByIdUseCase(1L) }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
