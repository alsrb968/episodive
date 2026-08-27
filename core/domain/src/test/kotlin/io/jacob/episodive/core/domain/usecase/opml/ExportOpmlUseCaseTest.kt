package io.jacob.episodive.core.domain.usecase.opml

import io.jacob.episodive.core.domain.datasource.OpmlFileDataSource
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsOnceUseCase
import io.jacob.episodive.core.model.opml.OpmlOutline
import io.jacob.episodive.core.model.opml.toOpmlOutline
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExportOpmlUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFollowedPodcastsOnceUseCase = mockk<GetFollowedPodcastsOnceUseCase>()
    private val opmlFileDataSource = mockk<OpmlFileDataSource>(relaxed = true)

    private val useCase = ExportOpmlUseCase(
        getFollowedPodcastsOnceUseCase = getFollowedPodcastsOnceUseCase,
        opmlFileDataSource = opmlFileDataSource,
    )

    @After
    fun teardown() {
        confirmVerified(getFollowedPodcastsOnceUseCase, opmlFileDataSource)
    }

    @Test
    fun `Given followed podcasts, when invoke called, then writes outlines and returns exported count`() =
        runTest {
            val followed = podcastTestDataList.take(3)
            coEvery { getFollowedPodcastsOnceUseCase() } returns followed
            val expectedOutlines = followed.map { it.toOpmlOutline() }

            val exportedCount = useCase("content://destination")

            // write 로 넘어간 outline 이 팔로우 목록을 그대로 반영해야 한다 — 순서·개수가
            // 어긋나면 사용자가 내보낸 파일에 자기가 구독한 것과 다른 목록이 담긴다.
            assertEquals(3, exportedCount)
            coVerifySequence {
                getFollowedPodcastsOnceUseCase()
                opmlFileDataSource.write("content://destination", expectedOutlines)
            }
        }

    @Test
    fun `Given no followed podcasts, when invoke called, then does not write and returns zero`() =
        runTest {
            coEvery { getFollowedPodcastsOnceUseCase() } returns emptyList()

            val exportedCount = useCase("content://destination")

            // 빈 파일을 만들면 사용자는 내보내기가 정상적으로 끝난 줄 안다.
            // 팔로우가 0개일 때는 write 를 아예 호출하지 않아야 한다.
            assertEquals(0, exportedCount)
            coVerify(exactly = 0) {
                opmlFileDataSource.write(any(), any<List<OpmlOutline>>())
            }
            coVerifySequence {
                getFollowedPodcastsOnceUseCase()
            }
        }
}
