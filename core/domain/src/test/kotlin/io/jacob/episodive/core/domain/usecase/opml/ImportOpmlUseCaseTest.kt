package io.jacob.episodive.core.domain.usecase.opml

import app.cash.turbine.test
import io.jacob.episodive.core.domain.datasource.OpmlFileDataSource
import io.jacob.episodive.core.domain.usecase.podcast.FollowPodcastUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastByFeedUrlUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastByGuidUseCase
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.DataErrorException
import io.jacob.episodive.core.model.opml.OpmlImportProgress
import io.jacob.episodive.core.model.opml.OpmlOutline
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImportOpmlUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val opmlFileDataSource = mockk<OpmlFileDataSource>()
    private val getPodcastByFeedUrlUseCase = mockk<GetPodcastByFeedUrlUseCase>()
    private val getPodcastByGuidUseCase = mockk<GetPodcastByGuidUseCase>()
    private val followPodcastUseCase = mockk<FollowPodcastUseCase>()

    private val useCase = ImportOpmlUseCase(
        opmlFileDataSource = opmlFileDataSource,
        getPodcastByFeedUrlUseCase = getPodcastByFeedUrlUseCase,
        getPodcastByGuidUseCase = getPodcastByGuidUseCase,
        followPodcastUseCase = followPodcastUseCase,
    )

    private val podcast = podcastTestDataList[0]

    private fun outline(title: String, xmlUrl: String? = "https://example.com/$title.xml", guid: String? = null) =
        OpmlOutline(title = title, xmlUrl = xmlUrl, guid = guid)

    @Test
    fun `Given outlines, when invoke called, then first emission carries only total and last emission is finished`() =
        runTest {
            val outlines = listOf(outline("A"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase(any()) } returns flowOf(podcast)
            coEvery { followPodcastUseCase(podcast.id) } returns true

            useCase("content://source").test {
                // 첫 방출은 total 만 채워진 초기 상태다. 여기서 done 이 미리 채워지면 화면이
                // 아직 시작도 안 한 항목을 처리 완료로 그린다.
                val initial = awaitItem()
                assertEquals(1, initial.total)
                assertEquals(0, initial.done)
                assertFalse(initial.isFinished)

                // 항목 하나를 처리한 직후의 방출은 아직 isFinished 가 아니다 — 완료 표시는
                // for 루프가 전부 끝난 뒤 별도로 한 번 더 방출된다.
                val perItemProgress = awaitItem()
                assertFalse(perItemProgress.isFinished)

                val finalProgress = awaitItem()
                // 완료 통지는 SharedFlow effect 가 아니라 상태 자체(isFinished)로 남는다 —
                // 화면이 재구독해도 완료 사실을 알 수 있어야 한다.
                assertTrue(finalProgress.isFinished)
                assertEquals(1, finalProgress.done)
                assertEquals(1, finalProgress.added)
                awaitComplete()
            }
        }

    @Test
    fun `Given lookup returns null, when invoke called, then counted as notFound not failed`() =
        runTest {
            val outlines = listOf(outline("Missing"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase(any()) } returns flowOf(null)

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // 항목 처리 직후 (isFinished=false)
                val finalProgress = awaitItem() // 루프 종료 후 최종 방출

                // notFound 와 failed 를 같은 통에 담으면 재시도해도 소용없는 항목과 재시도할
                // 만한 항목을 화면이 구분하지 못한다.
                assertEquals(1, finalProgress.notFound)
                assertTrue(finalProgress.failed.isEmpty())
                awaitComplete()
            }
            coVerify(exactly = 0) { followPodcastUseCase(any()) }
        }

    @Test
    fun `Given lookup throws, when invoke called, then counted as failed with title`() =
        runTest {
            val outlines = listOf(outline("Broken"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase(any()) } returns flow { throw RuntimeException("boom") }

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // 항목 처리 직후 (isFinished=false)
                val finalProgress = awaitItem() // 루프 종료 후 최종 방출

                assertEquals(0, finalProgress.notFound)
                assertEquals(listOf("Broken"), finalProgress.failed)
                awaitComplete()
            }
        }

    @Test
    fun `Given already followed, when invoke called, then counted as alreadyFollowed`() =
        runTest {
            val outlines = listOf(outline("Followed"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase(any()) } returns flowOf(podcast)
            coEvery { followPodcastUseCase(podcast.id) } returns false

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // 항목 처리 직후 (isFinished=false)
                val finalProgress = awaitItem() // 루프 종료 후 최종 방출

                assertEquals(1, finalProgress.alreadyFollowed)
                assertEquals(0, finalProgress.added)
                awaitComplete()
            }
        }

    @Test
    fun `Given middle item throws, when invoke called, then remaining items still processed`() =
        runTest {
            val outlines = listOf(outline("First"), outline("Second"), outline("Third"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase("https://example.com/First.xml") } returns flowOf(podcast)
            every { getPodcastByFeedUrlUseCase("https://example.com/Second.xml") } returns
                flow { throw RuntimeException("boom") }
            every { getPodcastByFeedUrlUseCase("https://example.com/Third.xml") } returns flowOf(podcast)
            coEvery { followPodcastUseCase(podcast.id) } returns true

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // First
                awaitItem() // Second (실패)
                awaitItem() // Third (isFinished=false)
                val finalProgress = awaitItem() // 루프 종료 후 최종 방출

                // 가운데 항목 하나가 던져도 마지막 항목까지 전부 처리돼야 한다 — 한 항목의
                // 실패로 나머지가 통째로 스킵되면 사용자는 이유 없이 빠진 구독을 의심하게 된다.
                assertEquals(3, finalProgress.done)
                assertEquals(2, finalProgress.added)
                assertEquals(listOf("Second"), finalProgress.failed)
                assertTrue(finalProgress.isFinished)
                awaitComplete()
            }
        }

    @Test
    fun `Given offline error, when invoke called, then stops without querying remaining items`() =
        runTest {
            val outlines = listOf(outline("First"), outline("Second"), outline("Third"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase("https://example.com/First.xml") } returns
                flow { throw DataErrorException(DataError.Offline) }

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                val finalProgress = awaitItem()

                // 오프라인이면 남은 항목도 같은 이유로 실패할 것이 뻔하다. 하나씩 failed 에
                // 쌓으면 "실패 187건" 처럼 보여 사용자가 파일 자체를 의심하게 된다.
                assertTrue(finalProgress.stoppedOffline)
                assertTrue(finalProgress.isFinished)
                assertTrue(finalProgress.failed.isEmpty())
                awaitComplete()
            }

            // 두 번째·세 번째 항목은 조회조차 되지 않아야 한다 — 오프라인 감지 즉시 끊는다는
            // 계약의 핵심이다.
            coVerify(exactly = 1) { getPodcastByFeedUrlUseCase(any()) }
            coVerify(exactly = 0) { getPodcastByGuidUseCase(any()) }
        }

    @Test
    fun `Given the feed url no longer resolves, when invoke called, then guid rescues the entry`() =
        runTest {
            // 이 폴백이 실제로 도는 유일한 경로다. 리더는 xmlUrl 없는 노드를 폴더로 보고
            // 목록에 넣지 않으므로, 가져오기가 받는 outline 은 xmlUrl 이 **항상** 차 있다.
            // 폴백 조건을 "xmlUrl 이 없을 때" 로 두면 guid 분기는 이 테스트 파일 안에서만
            // 도는 죽은 코드가 되고, 정작 필요한 경우 — 팟캐스트가 호스트를 옮겨 피드
            // 주소가 바뀌었지만 같은 파일의 guid 는 그대로 유효한 경우 — 를 놓친다.
            val outlines = listOf(
                outline("Moved", xmlUrl = "https://old-host.example.com/feed", guid = "stable-guid"),
            )
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase("https://old-host.example.com/feed") } returns flowOf(null)
            every { getPodcastByGuidUseCase("stable-guid") } returns flowOf(podcast)
            coEvery { followPodcastUseCase(podcast.id) } returns true

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // 항목 처리 직후
                val finalProgress = awaitItem() // 최종 방출

                assertEquals(1, finalProgress.added)
                assertEquals(0, finalProgress.notFound)
                awaitComplete()
            }
            coVerify { getPodcastByGuidUseCase("stable-guid") }
        }

    @Test
    fun `Given both lookups come back empty, when invoke called, then it is counted as notFound`() =
        runTest {
            val outlines = listOf(outline("Gone", xmlUrl = "https://gone.example.com/feed", guid = "gone-guid"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase(any()) } returns flowOf(null)
            every { getPodcastByGuidUseCase(any()) } returns flowOf(null)

            useCase("content://source").test {
                awaitItem()
                awaitItem()
                val finalProgress = awaitItem()

                assertEquals(1, finalProgress.notFound)
                assertEquals(0, finalProgress.added)
                awaitComplete()
            }
        }

    @Test
    fun `Given outline without xmlUrl, when invoke called, then falls back to guid lookup`() =
        runTest {
            val outlines = listOf(outline("ByGuid", xmlUrl = null, guid = "some-guid"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByGuidUseCase("some-guid") } returns flowOf(podcast)
            coEvery { followPodcastUseCase(podcast.id) } returns true

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // 항목 처리 직후 (isFinished=false)
                val finalProgress = awaitItem() // 루프 종료 후 최종 방출

                assertEquals(1, finalProgress.added)
                awaitComplete()
            }
            coVerify(exactly = 0) { getPodcastByFeedUrlUseCase(any()) }
        }

    @Test
    fun `Given outline without xmlUrl or guid, when invoke called, then notFound without querying`() =
        runTest {
            val outlines = listOf(outline("Neither", xmlUrl = null, guid = null))
            coEvery { opmlFileDataSource.read(any()) } returns outlines

            useCase("content://source").test {
                awaitItem() // 초기 진행률
                awaitItem() // 항목 처리 직후 (isFinished=false)
                val finalProgress = awaitItem() // 루프 종료 후 최종 방출

                assertEquals(1, finalProgress.notFound)
                awaitComplete()
            }
            coVerify(exactly = 0) { getPodcastByFeedUrlUseCase(any()) }
            coVerify(exactly = 0) { getPodcastByGuidUseCase(any()) }
        }

    @Test
    fun `Given file read fails, when invoke called, then flow terminates with exception not progress`() =
        runTest {
            val readError = RuntimeException("잘못된 XML")
            coEvery { opmlFileDataSource.read(any()) } throws readError

            // 파일을 읽는 단계의 실패는 진행률로 표현할 수 없다 — Flow 자체가 예외로
            // 끝나야 화면의 collect/catch 가 이를 직접 다룰 수 있다.
            useCase("content://source").test {
                val error = awaitError()
                assertEquals(readError, error)
            }
        }

    @Test
    fun `Given multiple emissions, when a later item fails, then earlier snapshots keep their own failed size`() =
        runTest {
            val outlines = listOf(outline("First"), outline("Second"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase("https://example.com/First.xml") } returns flowOf(podcast)
            every { getPodcastByFeedUrlUseCase("https://example.com/Second.xml") } returns
                flow { throw RuntimeException("boom") }
            coEvery { followPodcastUseCase(podcast.id) } returns true

            // 방출 순서: [0]=초기, [1]=First 처리 직후, [2]=Second 처리(실패) 직후,
            // [3]=루프 종료 후 최종(isFinished=true). 4개 모두 소비해야 awaitComplete 가 선다.
            val emissions = mutableListOf<OpmlImportProgress>()
            useCase("content://source").test {
                repeat(4) { emissions += awaitItem() }
                awaitComplete()
            }

            // First 처리 직후 방출된 스냅샷은 아직 실패가 없어야 한다. 구현이 같은
            // MutableList 인스턴스를 재사용해 나중에 담긴 실패로 과거 스냅샷까지 오염시키면
            // 이 스냅샷의 failed 도 나중에 1건으로 보이게 된다(리스트가 아직 비어 있지 않다).
            val afterFirst = emissions[1]
            assertEquals(0, afterFirst.failed.size)

            val afterSecond = emissions[2]
            assertEquals(listOf("Second"), afterSecond.failed)
        }

    @Test
    fun `Given item lookup cancelled, when invoke called, then cancellation propagates instead of being swallowed as failed`() =
        runTest {
            val outlines = listOf(outline("Cancelled"))
            coEvery { opmlFileDataSource.read(any()) } returns outlines
            every { getPodcastByFeedUrlUseCase(any()) } returns
                flow { throw CancellationException("cancelled") }

            // 취소를 failed 로 삼키면 화면이 사라진 뒤에도 남은 항목을 계속 돌고, 취소가
            // 호출자에게 전파되지 않는다.
            useCase("content://source").test {
                awaitItem() // 초기 진행률
                val error = awaitError()
                assertTrue(error is CancellationException)
            }
        }
}
