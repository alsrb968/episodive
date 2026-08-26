package io.jacob.episodive.core.domain.usecase.opml

import io.jacob.episodive.core.domain.datasource.OpmlFileDataSource
import io.jacob.episodive.core.domain.usecase.podcast.GetFollowedPodcastsOnceUseCase
import io.jacob.episodive.core.model.opml.toOpmlOutline
import javax.inject.Inject

class ExportOpmlUseCase @Inject constructor(
    private val getFollowedPodcastsOnceUseCase: GetFollowedPodcastsOnceUseCase,
    private val opmlFileDataSource: OpmlFileDataSource,
) {
    /**
     * [destinationUri] 에 팔로우 중인 팟캐스트를 OPML 로 내보낸다.
     *
     * @return 내보낸 팟캐스트 수. 팔로우가 하나도 없으면 빈 파일을 만들지 않고 그 자리에서
     * 0 을 돌려준다 — 화면이 이 값을 보고 스낵바로 안내한다.
     */
    suspend operator fun invoke(destinationUri: String): Int {
        val followedPodcasts = getFollowedPodcastsOnceUseCase()
        if (followedPodcasts.isEmpty()) return 0

        val outlines = followedPodcasts.map { it.toOpmlOutline() }
        opmlFileDataSource.write(destinationUri, outlines)
        return outlines.size
    }
}
