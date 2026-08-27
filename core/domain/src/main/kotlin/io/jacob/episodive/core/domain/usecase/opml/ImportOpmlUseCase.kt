package io.jacob.episodive.core.domain.usecase.opml

import io.jacob.episodive.core.domain.datasource.OpmlFileDataSource
import io.jacob.episodive.core.domain.usecase.podcast.FollowPodcastUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastByFeedUrlUseCase
import io.jacob.episodive.core.domain.usecase.podcast.GetPodcastByGuidUseCase
import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.opml.OpmlImportProgress
import io.jacob.episodive.core.model.opml.OpmlOutline
import io.jacob.episodive.core.model.asDataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImportOpmlUseCase @Inject constructor(
    private val opmlFileDataSource: OpmlFileDataSource,
    private val getPodcastByFeedUrlUseCase: GetPodcastByFeedUrlUseCase,
    private val getPodcastByGuidUseCase: GetPodcastByGuidUseCase,
    private val followPodcastUseCase: FollowPodcastUseCase,
) {
    /**
     * [sourceUri] 의 OPML 을 읽어 팔로우에 추가한다.
     *
     * 파일을 읽는 [OpmlFileDataSource.read] 자체가 던지는 예외(잘못된 XML 등)는 진행률로
     * 표현할 수 없으므로 여기서 감싸지 않는다 — 화면이 `collect`/`catch` 로 직접 잡아야 한다.
     * 그 뒤 항목별 처리 실패는 예외로 새지 않고 전부 [OpmlImportProgress] 로 접혀 나간다.
     *
     * 항목마다 「조회 → 즉시 팔로우」를 순차로 묶어서 처리한다. `getPodcastByFeedUrl` 은
     * `PodcastQuery.FeedUrl`(key=`"FEED_URL:$url"`)로 캐시를 채우는데, 그 저장 경로가
     * prefix 를 공유하는 캐시 표를 1,000행 초과 시 800행으로 트리밍한다. 조회를 전부 먼저
     * 끝내고 팔로우를 나중에 몰아 넣으면, 이번 가져오기 중 앞쪽에서 채운 캐시 행이 아직
     * 팔로우로 보호받기 전에 트리밍으로 지워질 수 있다 — 그 상태에서 팔로우를 넣으면 FK
     * 제약 위반(`SQLiteConstraintException`)이 난다. 건별로 묶으면 그 창이 마이크로초로
     * 줄고, 팔로우 행 자체가 이후 트리밍으로부터의 보호막이 된다. 이 창을 지키기 위해
     * 병렬화하지 않고 순차로 처리한다 — 어차피 SQLite 는 단일 라이터라 쓰기는 직렬화된다.
     */
    operator fun invoke(sourceUri: String): Flow<OpmlImportProgress> = flow {
        val outlines = opmlFileDataSource.read(sourceUri)

        var done = 0
        var added = 0
        var alreadyFollowed = 0
        var notFound = 0
        val failed = mutableListOf<String>()

        emit(OpmlImportProgress(total = outlines.size))

        for (outline in outlines) {
            try {
                val podcast = findPodcast(outline)
                if (podcast == null) {
                    notFound++
                } else if (followPodcastUseCase(podcast.id)) {
                    added++
                } else {
                    alreadyFollowed++
                }
                done++
            } catch (e: CancellationException) {
                // 취소는 실패가 아니다. 여기서 삼키면 화면이 사라진 뒤에도 남은 항목을
                // 계속 돌고, 취소가 위로 전파되지 않는다. (`RemoteUpdater.refreshIfNeeded`
                // 가 같은 이유로 같은 처리를 한다.)
                throw e
            } catch (e: Exception) {
                if (e.asDataError() is DataError.Offline) {
                    // 남은 항목도 전부 같은 이유로 실패할 것이 뻔하다 — 하나씩 failed 에
                    // 쌓지 않고 여기서 끊는다.
                    emit(
                        OpmlImportProgress(
                            total = outlines.size,
                            done = done,
                            added = added,
                            alreadyFollowed = alreadyFollowed,
                            notFound = notFound,
                            failed = failed.toList(),
                            isFinished = true,
                            stoppedOffline = true,
                        )
                    )
                    return@flow
                }
                failed += outline.title
                done++
            }

            emit(
                OpmlImportProgress(
                    total = outlines.size,
                    done = done,
                    added = added,
                    alreadyFollowed = alreadyFollowed,
                    notFound = notFound,
                    failed = failed.toList(),
                )
            )
        }

        emit(
            OpmlImportProgress(
                total = outlines.size,
                done = done,
                added = added,
                alreadyFollowed = alreadyFollowed,
                notFound = notFound,
                failed = failed.toList(),
                isFinished = true,
            )
        )
    }

    /**
     * xmlUrl 로 먼저 찾고, **그것이 빈손으로 돌아오면** guid 로 한 번 더 찾는다.
     *
     * 폴백 조건이 "xmlUrl 이 없을 때" 가 아니라 "xmlUrl 로 못 찾았을 때" 인 것이 중요하다.
     * [OpmlFileDataSource] 가 돌려주는 outline 은 xmlUrl 이 **항상** 차 있다 — 리더가
     * xmlUrl 없는 노드를 폴더로 보고 목록에 넣지 않기 때문이다. 그래서 "없을 때" 로 두면
     * guid 분기는 단위 테스트에서만 도는 죽은 코드가 되고, 정작 폴백이 필요한 경우를
     * 놓친다: 팟캐스트가 호스트를 옮겨 피드 주소가 바뀌면(흔하다) 우리가 내보낸 파일의
     * xmlUrl 은 더 이상 맞지 않는데, 같은 파일에 실려 있는 guid 는 그대로 유효하다.
     *
     * 두 값을 지역 변수로 받아 두는 것은 `:core:model` 이 다른 모듈이라 프로퍼티 접근만으로는
     * 스마트 캐스트가 걸리지 않기 때문이다.
     */
    private suspend fun findPodcast(outline: OpmlOutline): Podcast? {
        val xmlUrl = outline.xmlUrl
        val guid = outline.guid

        val byFeedUrl = xmlUrl?.let { getPodcastByFeedUrlUseCase(it).first() }
        if (byFeedUrl != null) return byFeedUrl

        return guid?.let { getPodcastByGuidUseCase(it).first() }
    }
}
