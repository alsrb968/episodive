package io.jacob.episodive.feature.home.navigation

import kotlinx.serialization.Serializable

/**
 * 홈 바텀시트 안의 섹션. '더 보기' 목적지를 route 인자로 실어 나른다.
 *
 * sealed 가 아니라 enum 인 이유는 섹션마다 추가로 실을 데이터가 없기 때문이다. 이 값 하나로
 * 제목·레이아웃·데이터 소스가 전부 결정된다. exhaustive `when` 이 강제되므로 섹션을 추가할
 * 때 매핑을 빠뜨리면 컴파일이 깨진다.
 *
 * 시트 밖의 '이어듣기' 캐러셀은 섹션이 아니라 히어로 영역이라 여기 없다.
 */
@Serializable
enum class HomeSection {
    MyRecentPodcasts,
    RandomEpisodes,
    MyTrendingPodcasts,
    FollowedPodcasts,
    LocalTrendingPodcasts,
    ForeignTrendingPodcasts,
    LiveEpisodes,
    Channels,
}

/** 전체 목록 화면이 항목을 늘어놓는 방식. */
enum class HomeMoreLayout {
    /** 커버가 주인공인 그리드. 열 수는 화면이 정한다. */
    PodcastGrid,

    /** 제목·메타가 주인공인 세로 리스트. */
    EpisodeList,

    /** 채널 카드 그리드. 설명이 세 줄까지 들어가 팟캐스트보다 열이 적다. */
    ChannelGrid,
}

val HomeSection.layout: HomeMoreLayout
    get() = when (this) {
        HomeSection.RandomEpisodes, HomeSection.LiveEpisodes -> HomeMoreLayout.EpisodeList
        HomeSection.Channels -> HomeMoreLayout.ChannelGrid
        HomeSection.MyRecentPodcasts,
        HomeSection.MyTrendingPodcasts,
        HomeSection.FollowedPodcasts,
        HomeSection.LocalTrendingPodcasts,
        HomeSection.ForeignTrendingPodcasts,
            -> HomeMoreLayout.PodcastGrid
    }
