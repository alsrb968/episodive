package io.jacob.episodive.feature.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.feature.home.navigation.HomeSection
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 겹친 제목 띠의 **터치 차단이 배경 알파와 같은 시점에 켜지고 꺼지는지** 확인한다.
 *
 * 차단을 `titleVisible`(즉시 뒤집히는 불리언)에 걸면 배경 페이드(220ms)와 어긋난다. 사라지는
 * 동안엔 눈에 멀쩡히 보이는 띠가 터치를 아래 항목으로 흘려보내고 — 사용자는 누른 적 없는
 * 팟캐스트로 넘어간다 — 나타나는 동안엔 아직 비어 있는 띠가 터치를 삼킨다.
 *
 * [HomeMoreScreenTest] 와 나눠 둔 이유는 애니메이션이다. 그쪽은 `ANIMATOR_DURATION_SCALE` 을
 * 0 으로 낮춰 페이드를 즉시 끝내므로 검사할 중간 구간 자체가 없다. 여기서는 배율을 건드리지
 * 않고 테스트 시계를 직접 돌려 전환 한복판을 붙잡는다. 이미 로드된 PagingData 만 쓰므로
 * 스켈레톤 shimmer 가 `waitForIdle` 을 붙잡는 문제도 없다.
 */
@RunWith(RobolectricTestRunner::class)
class HomeMoreHeaderTimingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** 영문 기본 리소스(`feature_home_section_followed_podcasts`)가 만들어내는 문자열. */
    private val followedPodcastsTitle = "Followed podcasts"

    private var clickedId: Long? = null

    @Test
    fun whileTheBandIsStillFadingOut_itKeepsSwallowingTouches() {
        hideTitleThenWait(MidTransitionMs)

        // 전환 한복판임을 먼저 못박는다. AnimatedVisibility 는 exit 가 끝나야 노드를 걷어
        // 가므로, 제목 노드가 아직 있다는 것이 곧 "배경도 아직 남아 있다"는 뜻이다. 이
        // 단언이 없으면 애니가 이미 끝난 상태를 성공으로 착각한다.
        composeTestRule.onNodeWithText(followedPodcastsTitle).assertExists()

        clickBandCenter()

        assertNull("아직 칠해져 있는 띠가 터치를 아래 항목으로 흘려보냈다", clickedId)
    }

    @Test
    fun onceTheBandIsGone_touchesReachTheItemsBeneath() {
        // 위 테스트의 양성 대조군이다. 이것이 없으면 "클릭이 아예 도달할 수 없는" 어떤
        // 변경도 위 테스트를 통과시킨다 — 차단을 상시로 되돌리는 회귀가 정확히 그렇다.
        hideTitleThenWait(SettleMs)

        composeTestRule.onNodeWithText(followedPodcastsTitle).assertDoesNotExist()

        clickBandCenter()

        // 어느 항목이 그 자리에 오는지는 그리드 열 수와 스와이프 거리에 달렸다. 특정 id 를
        // 박아 두면 화면 크기 한정자가 바뀌는 순간 이 테스트가 무너진다 — 여기서 볼 것은
        // "띠가 걷힌 뒤에는 아래로 터치가 닿는다"뿐이다.
        assertNotNull("제목이 물러난 자리의 항목이 눌리지 않았다", clickedId)
    }

    /** 화면을 띄우고 제목을 물러나게 한 뒤 [waitMs] 만큼 시계를 돌린다. */
    private fun hideTitleThenWait(waitMs: Long) {
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            EpisodiveTheme {
                HomeMoreScreen(
                    section = HomeSection.FollowedPodcasts,
                    content = HomeMoreContent.PodcastPaging(
                        flowOf(PagingData.from(scrollablePodcasts()))
                    ),
                    onBackClick = {},
                    onPodcastClick = { clickedId = it },
                    onChannelClick = {},
                    onPlayEpisode = {},
                    onToggleLikedEpisode = {},
                    onToggleSavedEpisode = {},
                    onRetry = {},
                )
            }
        }

        // 첫 조합·레이아웃을 끝내 목록이 자리를 잡게 한다.
        composeTestRule.mainClock.advanceTimeBy(SettleMs)

        // 속도를 0 으로 끝내 플링을 없앤다. 플링이 남으면 목록이 아직 굴러가는 중이라
        // 탭이 스크롤 제스처에 먹혀, 차단 여부와 무관하게 클릭이 실패한다 — 그러면 이
        // 테스트는 무엇을 재는지 알 수 없게 된다.
        // 스와이프 자체도 시계를 그 길이만큼 밀기 때문에 짧게 잡는다. 200ms 로 쓸면
        // 페이드 220ms 가 스와이프 안에서 거의 끝나 버려 중간 구간이 남지 않는다.
        composeTestRule.onRoot().performTouchInput {
            swipeWithVelocity(
                start = Offset(center.x, center.y + SwipeDistancePx),
                end = Offset(center.x, center.y - SwipeDistancePx),
                endVelocity = 0f,
                durationMillis = SwipeDurationMs,
            )
        }

        composeTestRule.mainClock.advanceTimeBy(waitMs)
    }

    private fun clickBandCenter() {
        val bandCenterY = with(composeTestRule.density) { CollapsedBandCenterDp.dp.toPx() }
        composeTestRule.onRoot().performTouchInput {
            click(Offset(center.x, bandCenterY))
        }
        composeTestRule.mainClock.advanceTimeBy(SettleMs)
    }

    /** 한 화면을 넘겨 스크롤이 가능할 만큼의 목록. 그리드 키가 겹치면 터지므로 id 를 새로 준다. */
    private fun scrollablePodcasts() = List(60) { index ->
        podcastTestDataList[index % podcastTestDataList.size].copy(id = index.toLong())
    }
}

/** 조합·레이아웃과 애니메이션이 완전히 끝나기에 넉넉한 시간(ms). */
private const val SettleMs = 1_000L

/**
 * 숨김 애니메이션 한복판(ms).
 *
 * 띠의 페이드는 220ms 이고 스와이프가 이미 [SwipeDurationMs] 를 먹었으므로, 여기까지 와도
 * 배경은 아직 절반쯤 남아 있다. 차단을 불리언에 걸어 두면 바로 이 구간이 새어 나간다.
 */
private const val MidTransitionMs = 110L

/** 제목 숨김 임계값(48dp)을 넉넉히 넘기는 스와이프 거리(px). */
private const val SwipeDistancePx = 300f

/** 스와이프 길이(ms). 시계를 그만큼 밀므로 페이드 220ms 안에 충분히 들어가는 값으로 잡는다. */
private const val SwipeDurationMs = 50L

/** 접힌 제목 띠(64dp)의 세로 중앙. Robolectric 은 상태바 인셋이 0 이라 화면 맨 위에서 시작한다. */
private const val CollapsedBandCenterDp = 32

