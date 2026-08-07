package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun EpisodiveScaffold(
    modifier: Modifier = Modifier,
    title: String,
    /**
     * 기본값은 탭 루트용 오버사이즈 제목이다. 섹션에서 파고든 화면처럼 제목이 콘텐츠보다
     * 커 보이면 안 되는 곳은 더 작은 스타일을 넘긴다.
     */
    titleStyle: TextStyle = MaterialTheme.typography.displaySmall,
    /**
     * true 면 아래로 스크롤할 때 제목이 위로 밀려 사라지고, 살짝만 되올려도 다시 내려온다.
     *
     * 내비게이션 아이콘은 이 값과 무관하게 그대로 남는다. 돌아갈 길까지 같이 숨기면
     * 사용자는 나가려고 목록을 위로 되감아야 하고, 그건 스크롤 위치가 정할 일이 아니다.
     *
     * 켜면 상단이 콘텐츠 **위에 겹쳐** 그려진다. 목록은 [content] 가 받은 여백을
     * `Modifier.padding` 이 아니라 **contentPadding** 으로 써야 한다 — 컨테이너를 줄이면
     * 겹쳐 놓은 의미가 없어져, 제목이 사라져도 그 자리에 빈 띠가 남는다.
     *
     * [subTitle] 은 겹친 헤더 안에 그대로 들어간다. 배경은 제목 띠에만 칠해지므로,
     * 이 모드에서 배경이 필요한 subTitle 을 쓰려면 그쪽에서 직접 칠해야 한다.
     */
    hideTitleOnScroll: Boolean = false,
    subTitle: @Composable () -> Unit = {},
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    content: @Composable (PaddingValues, NestedScrollConnection) -> Unit,
) {
    val density = LocalDensity.current
    // 목록 위치는 복원되는데 제목만 되살아나면, 회전 뒤 불투명한 제목 띠가 목록 한복판을
    // 덮은 채 나타난다. 스크롤 상태와 같이 저장한다.
    val titleVisible = rememberSaveable { mutableStateOf(true) }
    val titleVisibilityConnection = remember(density) {
        TitleVisibilityConnection(
            visible = titleVisible,
            hideThresholdPx = with(density) { TitleHideThreshold.toPx() },
            showThresholdPx = with(density) { TitleShowThreshold.toPx() },
        )
    }

    Scaffold(
        // 스크롤 이벤트는 콘텐츠에서 위로 전파되므로, 여기 달아 두면 화면마다 연결을
        // 따로 배선하지 않아도 된다. 끄면 아예 붙이지 않아 나머지 화면에는 비용이 없다.
        modifier = if (hideTitleOnScroll) {
            modifier.nestedScroll(titleVisibilityConnection)
        } else {
            modifier
        },
        topBar = {
            // 감춤 모드에서는 이 슬롯을 비우고 콘텐츠 위에 겹쳐 그린다. 슬롯에 두면 제목이
            // 사라져도 그 높이가 그대로 남아, 제목만 빠지고 빈 띠가 버티는 모습이 된다.
            if (hideTitleOnScroll) return@Scaffold

            Column {
                EpisodiveTopAppBar(
                    title = {
                        Text(
                            // 탭 루트의 화면 제목은 34/800/-.03em (원본 줄 232, 485).
                            // headlineMedium(28)은 한 단계 작아서 v2 의 오버사이즈 타이포
                            // 대비가 죽는다 — 그래서 기본값이 displaySmall 이다.
                            text = title,
                            style = titleStyle,
                        )
                    },
                    navigationIcon = navigationIcon,
                    navigationIconContentDescription = navigationIconContentDescription,
                    actionIcon = actionIcon,
                    actionIconContentDescription = actionIconContentDescription,
                    onNavigationClick = onNavigationClick,
                    onActionClick = onActionClick,
                    scrollBehavior = scrollBehavior
                )

                subTitle()
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars)
    ) { paddingValues ->
        if (hideTitleOnScroll) {
            CollapsingTitleContent(
                paddingValues = paddingValues,
                titleVisible = titleVisible.value,
                title = title,
                titleStyle = titleStyle,
                subTitle = subTitle,
                navigationIcon = navigationIcon,
                navigationIconContentDescription = navigationIconContentDescription,
                actionIcon = actionIcon,
                actionIconContentDescription = actionIconContentDescription,
                onNavigationClick = onNavigationClick,
                onActionClick = onActionClick,
                scrollBehavior = scrollBehavior,
                content = content,
            )
        } else {
            content(
                paddingValues,
                scrollBehavior.nestedScrollConnection
            )
        }
    }
}

/**
 * 탑바를 콘텐츠 **위에 겹쳐** 그리고, 제목과 배경을 함께 여닫는다.
 *
 * 겹치는 것이 핵심이다. Scaffold 의 topBar 슬롯은 높이를 차지하므로, 거기 두고 제목만
 * 숨기면 그 높이가 빈 띠로 남는다. 겹쳐 두면 목록이 그 자리까지 올라와 화면을 다 쓴다.
 *
 * 콘텐츠에는 탑바 높이만큼의 **contentPadding** 을 넘긴다 — 컨테이너 패딩이 아니다. 그래야
 * 처음에는 제목 아래에서 시작하면서도, 스크롤하면 항목이 그 자리를 지나 올라간다.
 *
 * 배경은 제목과 같은 타이밍으로 사라진다. 남겨 두면 목록 한복판에서 제목을 되불렀을 때
 * 글자가 아래 항목과 겹쳐 읽히지 않는다.
 */
@Composable
private fun CollapsingTitleContent(
    paddingValues: PaddingValues,
    titleVisible: Boolean,
    title: String,
    titleStyle: TextStyle,
    subTitle: @Composable () -> Unit,
    navigationIcon: ImageVector?,
    navigationIconContentDescription: String?,
    actionIcon: ImageVector?,
    actionIconContentDescription: String?,
    onNavigationClick: () -> Unit,
    onActionClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    content: @Composable (PaddingValues, NestedScrollConnection) -> Unit,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val scrimColor = MaterialTheme.colorScheme.surface
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Scaffold 의 topBar 슬롯이 비어 있어 paddingValues.top 을 믿을 수 없다. 겹친 헤더를
    // 직접 재고, 재기 전 한 프레임을 위해 어림값을 둔다.
    var headerHeight by remember(statusBarHeight) {
        mutableStateOf(statusBarHeight + CollapsingTopBarHeight)
    }

    // 배경과 스크림이 이 값 하나에서 나오고, 제목도 같은 스펙을 쓴다. 제각기 두면 서로
    // 다른 스프링으로 움직여 제목이 배경보다 먼저 돌아오는 구간이 생긴다.
    // `by` 로 풀지 않는 것은 draw 단계에서 읽어 재구성 대신 다시 그리기만 시키기 위해서다.
    val headerProgress = animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(CollapsingHeaderDurationMs),
        label = "collapsingHeader",
    )

    Box {
        content(
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = headerHeight,
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding(),
            ),
            scrollBehavior.nestedScrollConnection,
        )

        Column(
            modifier = Modifier.onSizeChanged {
                // 펼쳐진 높이만 기록한다. 접힌 높이를 반영하면 콘텐츠 여백이 같이 줄어
                // 목록이 위로 딸려 올라갔다가 제목이 돌아올 때 도로 밀린다.
                if (titleVisible) headerHeight = with(density) { it.height.toDp() }
            },
        ) {
            // 상태바 자리는 늘 배경을 깐다. 물러나는 것은 앱이 그린 제목 띠지 시스템 크롬이
            // 아니다 — 여기까지 비우면 시계와 배터리가 앨범아트 위에 놓여 읽히지 않는다.
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .background(backgroundColor),
            )

            CollapsingHeaderBand(
                titleVisible = titleVisible,
                progress = headerProgress,
                title = title,
                titleStyle = titleStyle,
                backgroundColor = backgroundColor,
                scrimColor = scrimColor,
                navigationIcon = navigationIcon,
                navigationIconContentDescription = navigationIconContentDescription,
                actionIcon = actionIcon,
                actionIconContentDescription = actionIconContentDescription,
                onNavigationClick = onNavigationClick,
                onActionClick = onActionClick,
            )

            subTitle()
        }
    }
}

/**
 * 겹친 헤더의 제목 띠.
 *
 * [CollapsingTitleContent] 안에 인라인으로 두지 않고 떼어 낸 것은 바깥 Column 의
 * `ColumnScope` 가 [AnimatedVisibility] 의 ColumnScope 오버로드를 끌어당겨, Box 안에서
 * 호출해도 그쪽으로 해석되기 때문이다.
 */
@Composable
private fun CollapsingHeaderBand(
    titleVisible: Boolean,
    progress: State<Float>,
    title: String,
    titleStyle: TextStyle,
    backgroundColor: Color,
    scrimColor: Color,
    navigationIcon: ImageVector?,
    navigationIconContentDescription: String?,
    actionIcon: ImageVector?,
    actionIconContentDescription: String?,
    onNavigationClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    // 배경이 물러난 만큼 스크림이 짙어진다. 둘을 따로 켜고 끄면 전환 도중 아이콘이
    // 아무 바탕 없이 앨범아트 위에 놓이는 구간이 생긴다.
    val iconScrim = scrimColor.copy(alpha = CollapsingIconScrimAlpha * (1f - progress.value))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CollapsingTopBarHeight)
            // 제목이 위로 빠져나가는 것을 이 띠 안에서 잘라 낸다.
            .clipToBounds()
            // 배경이 떠 있는 동안만 터치를 막는다. M3 TopAppBar 를 그대로 겹치면 그쪽이
            // 조건 없이 pointerInput 을 달아, 배경이 물러난 뒤에도 이 띠가 통째로 터치를
            // 삼킨다 — 그 자리까지 올라온 항목이 눈에는 멀쩡히 보이는데 눌리지도 끌리지도
            // 않는 죽은 띠가 된다.
            .then(if (titleVisible) Modifier.pointerInput(Unit) {} else Modifier)
            .drawBehind { drawRect(backgroundColor.copy(alpha = progress.value)) },
    ) {
        if (navigationIcon != null && navigationIconContentDescription != null) {
            CollapsingHeaderIcon(
                modifier = Modifier.align(Alignment.CenterStart),
                icon = navigationIcon,
                contentDescription = navigationIconContentDescription,
                scrim = iconScrim,
                onClick = onNavigationClick,
            )
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = CollapsingTitleInset, end = CollapsingBarPadding),
            visible = titleVisible,
            // 배경 알파와 **같은 길이**를 명시한다. 기본값에 맡기면 이쪽은 스프링, 저쪽은
            // 다른 스프링이라 제목이 배경보다 먼저 돌아오고, 그 몇 프레임 동안 글자가
            // 아무 바탕 없이 앨범아트 위에 놓인다.
            enter = slideInVertically(tween(CollapsingHeaderDurationMs)) { -it } +
                    fadeIn(tween(CollapsingHeaderDurationMs)),
            exit = slideOutVertically(tween(CollapsingHeaderDurationMs)) { -it } +
                    fadeOut(tween(CollapsingHeaderDurationMs)),
        ) {
            Text(
                text = title,
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (actionIcon != null && actionIconContentDescription != null) {
            CollapsingHeaderIcon(
                modifier = Modifier.align(Alignment.CenterEnd),
                icon = actionIcon,
                contentDescription = actionIconContentDescription,
                scrim = iconScrim,
                onClick = onActionClick,
            )
        }
    }
}

/** 겹친 헤더의 아이콘. 배경이 물러났을 때 목록 위에서도 읽히도록 원형 스크림을 깐다. */
@Composable
private fun CollapsingHeaderIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    scrim: Color,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .padding(horizontal = CollapsingBarPadding)
            .size(CollapsingIconButtonSize)
            .clip(CircleShape)
            .background(scrim),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(CollapsingIconSize),
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 스크롤 **방향**으로 제목 노출을 정하는 연결.
 *
 * 절대 위치(예: [FadeTopBarLayout] 의 offset)로 판단하지 않는 이유는, 목록 아래쪽에 한참
 * 내려가 있는 상태에서도 조금만 되올리면 제목이 돌아와야 하기 때문이다. 위치로 보면
 * 그때는 계속 숨은 채다.
 *
 * 판단은 `available` 이 아니라 `consumed` 로 한다. 목록 맨 위에서 더 당길 때 남는 델타가
 * `available` 에는 그대로 실려서, 그것까지 방향으로 읽으면 제목이 헛돈다.
 */
private class TitleVisibilityConnection(
    private val visible: MutableState<Boolean>,
    private val hideThresholdPx: Float,
    private val showThresholdPx: Float,
) : NestedScrollConnection {
    private var accumulated = 0f

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = consumed.y
        if (delta == 0f) {
            // 목록이 못 움직였다. 끝에 닿았거나, 화면보다 짧아져 아예 스크롤이 불가능해진
            // 것이다. 후자에서 숨은 채로 두면 제목을 되부를 방법이 영영 없다 — 되돌리는
            // 유일한 입력이 스크롤인데 그 스크롤이 성립하지 않는다.
            if (available.y != 0f) visible.value = true
            return Offset.Zero
        }

        // 방향이 바뀌면 누적을 버린다. 그러지 않으면 한참 내려간 뒤 되올릴 때 그동안 쌓인
        // 음수에 묻혀, 임계값을 넘기려고 한참을 더 올려야 한다.
        if ((delta > 0f) != (accumulated > 0f)) accumulated = 0f
        accumulated += delta

        when {
            accumulated <= -hideThresholdPx -> {
                visible.value = false
                accumulated = 0f
            }

            accumulated >= showThresholdPx -> {
                visible.value = true
                accumulated = 0f
            }
        }

        return Offset.Zero
    }
}

/**
 * 겹친 헤더가 여닫히는 속도.
 *
 * 배경 알파와 제목이 **같은** 것을 써야 한다. 한쪽만 기본 스프링에 맡기면 둘의 궤적이
 * 달라져, 제목이 배경보다 먼저 도착하는 몇 프레임 동안 글자가 목록 위에 맨몸으로 뜬다.
 */
private const val CollapsingHeaderDurationMs = 220

/** 겹친 헤더의 제목 띠 높이. M3 small TopAppBar 의 컨테이너 높이와 같게 맞춘다. */
private val CollapsingTopBarHeight = 64.dp

/** 아이콘을 띠 가장자리에서 띄우는 여백. M3 TopAppBar 의 수평 패딩과 같다. */
private val CollapsingBarPadding = 4.dp

/** 아이콘 버튼과 아이콘 크기. [EpisodiveTopAppBar] 가 쓰는 값을 그대로 따른다. */
private val CollapsingIconButtonSize = 40.dp
private val CollapsingIconSize = 20.dp

/** 제목이 시작하는 위치. 좌측 아이콘 자리(여백 + 버튼)를 비켜 선다. */
private val CollapsingTitleInset = CollapsingBarPadding * 2 + CollapsingIconButtonSize

/**
 * 배경이 물러났을 때 아이콘을 띄우는 원의 진하기.
 *
 * 공용 [EpisodiveTopAppBarDefaults.navigationIconScrim] (0.32)보다 진하다. 그쪽은 히어로
 * 이미지 한 장 위에 놓이는 것을 상정했지만, 여기서는 밝은 앨범아트가 격자로 깔려 32%로는
 * 원이 보이지 않는다. 나가는 유일한 길이라 안 보이면 화면에 갇힌다.
 */
private const val CollapsingIconScrimAlpha = 0.72f

/** 제목을 숨기는 데 필요한 아래 방향 스크롤. 살짝 건드린 정도로는 사라지지 않게 넉넉히 준다. */
private val TitleHideThreshold = 48.dp

/** 제목을 되돌리는 데 필요한 위 방향 스크롤. 숨기는 쪽보다 훨씬 짧다 — 되찾기는 쉬워야 한다. */
private val TitleShowThreshold = 8.dp

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    onActionClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val dimension = LocalDimensionTheme.current
    val hasAction = actionIcon != null && actionIconContentDescription != null

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SectionHeaderMinHeight)
                // 제목까지 통째로 누를 수 있게 한다. 화살표만 눌리면 정작 가장 크고 눈에
                // 먼저 들어오는 제목이 죽은 영역이 되고, 사용자는 그걸 몇 번 눌러 본 뒤에야
                // 옆의 작은 아이콘을 찾는다. clickable 을 padding 보다 앞에 둬서 좌우 여백과
                // 리플이 화면 끝까지 닿는다.
                .then(
                    if (hasAction) Modifier.clickable(onClick = onActionClick) else Modifier
                )
                .padding(horizontal = dimension.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (actionIcon != null && actionIconContentDescription != null) {
                // IconButton 이 아니라 Icon 이다. 행 전체가 이미 클릭 대상이라 버튼을 겹쳐
                // 두면 클릭 지점이 둘로 갈리고 스크린리더에도 같은 동작이 두 번 읽힌다.
                Icon(
                    modifier = Modifier.size(SectionHeaderActionIconSize),
                    imageVector = actionIcon,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = actionIconContentDescription
                )
            }
        }

        Spacer(modifier = Modifier.height(SectionHeaderContentSpacing))

        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * [SectionHeader] 의 제목 자리.
 *
 * 좌우 여백과 제목-콘텐츠 간격을 실제 헤더와 같은 상수에서 가져온다. 값을 베껴 두면 헤더를
 * 손볼 때 스켈레톤만 남아 전환할 때마다 몇 px 씩 튄다.
 *
 * @param hasAction 실제 헤더에 더 보기 같은 액션이 붙는 자리면 true. 액션이 붙은 헤더는
 *   최소 높이가 커지므로, 그 높이를 미리 잡아 두지 않으면 데이터가 채워지는 순간 아래
 *   콘텐츠가 통째로 밀린다.
 */
@Composable
fun SectionHeaderSkeleton(
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    titleWidthFraction: Float = SectionHeaderSkeletonTitleWidth,
    hasAction: Boolean = false,
) {
    val dimension = LocalDimensionTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hasAction) Modifier.heightIn(min = SectionHeaderMinHeight) else Modifier
                )
                .padding(horizontal = dimension.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonLine(
                modifier = Modifier.weight(1f),
                style = titleStyle,
                widthFraction = titleWidthFraction,
            )

            if (hasAction) {
                // 자리만 비워 둔다. 더 보기 화살표는 데이터와 무관한 정적 크롬이라, 여기서
                // 함께 반짝이면 그것도 로딩 중인 것처럼 읽힌다.
                Spacer(modifier = Modifier.size(SectionHeaderActionIconSize))
            }
        }

        Spacer(modifier = Modifier.height(SectionHeaderContentSpacing))
    }
}

private val SectionHeaderContentSpacing = 14.dp
private const val SectionHeaderSkeletonTitleWidth = 0.4f

/**
 * 액션이 붙은 [SectionHeader] 의 최소 높이. 제목 한 줄만으로는 터치 타깃이 얕아서,
 * 행 전체가 클릭 대상인 만큼 손가락이 닿을 높이를 확보한다. 스켈레톤도 같은 값을 쓴다.
 */
private val SectionHeaderMinHeight = 48.dp

/** [SectionHeader] 우측 화살표 크기. 스켈레톤이 예약하는 자리도 이 값을 쓴다. */
private val SectionHeaderActionIconSize = 24.dp

@Composable
fun SubSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            // 대제목(SectionHeader)은 좌우 20dp 다. 여기만 사방 16dp 면 같은 화면에서
            // 두 헤더의 좌측 선이 4dp 어긋나고, 디자인에 없는 상하 여백까지 붙는다.
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LocalDimensionTheme.current.screenPadding),
            text = title,
            // 원본 소제목은 14~15/700 (원본 줄 235·266).
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun FadeTopBarLayout(
    modifier: Modifier = Modifier,
    state: LazyListState,
    offset: Int = 700,
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val showTopBar by remember {
        derivedStateOf {
            val firstVisibleItem = state.firstVisibleItemIndex > 0
            val offsetPastFirst = state.firstVisibleItemIndex == 0 &&
                    state.firstVisibleItemScrollOffset > offset
            firstVisibleItem || offsetPastFirst
        }
    }

    FadeTopBarLayoutContent(
        modifier = modifier,
        showTopBar = showTopBar,
        title = title,
        onBack = onBack,
        content = content
    )
}

@Composable
fun FadeTopBarLayout(
    modifier: Modifier = Modifier,
    state: LazyGridState,
    offset: Int = 700,
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val showTopBar by remember {
        derivedStateOf {
            val firstVisibleItem = state.firstVisibleItemIndex > 0
            val offsetPastFirst = state.firstVisibleItemIndex == 0 &&
                    state.firstVisibleItemScrollOffset > offset
            firstVisibleItem || offsetPastFirst
        }
    }

    FadeTopBarLayoutContent(
        modifier = modifier,
        showTopBar = showTopBar,
        title = title,
        onBack = onBack,
        content = content
    )
}

@Composable
private fun FadeTopBarLayoutContent(
    modifier: Modifier = Modifier,
    showTopBar: Boolean,
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()

        EpisodiveCenterTopAppBar(
            modifier = Modifier,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (showTopBar) 1f else 0f)
            ),
            iconButtonColors = IconButtonDefaults.iconButtonColors(
                // 버튼 뒤 원은 EpisodiveCenterTopAppBar 가 navigationIconScrim 으로 그린다.
                // 여기서 또 칠하면 두 겹이 된다.
                containerColor = Color.Transparent,
            ),
            // 스크롤로 탑바가 채워지면 뒤에 비칠 배경이 없으므로 원도 지운다.
            navigationIconScrim = if (showTopBar) {
                Color.Transparent
            } else {
                EpisodiveTopAppBarDefaults.navigationIconScrim()
            },
            title = {
                AnimatedVisibility(
                    visible = showTopBar,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = EpisodiveIcons.ArrowBack,
            navigationIconContentDescription = "Back",
            onNavigationClick = onBack,
        )
    }
}

@DevicePreviews
@Composable
private fun EpisodiveScaffoldPreview() {
    EpisodiveTheme {
        EpisodiveScaffold(
            title = "Title",
        ) { paddingValues, nestedScrollConnection ->
            Text(
                text = "Content",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun SectionHeaderPreview() {
    EpisodiveTheme {
        SectionHeader(
            title = "Preview",
            actionIcon = EpisodiveIcons.CaretRight,
            actionIconContentDescription = "See All",
            onActionClick = {}
        ) {
            Text(
                text = "Content",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * 액션이 붙은 헤더와 그 스켈레톤을 위아래로 두어 헤더 높이가 같은지 눈으로 확인한다.
 * 어긋나면 로딩에서 실제로 넘어갈 때 아래 콘텐츠가 그만큼 밀린다.
 */
@ThemePreviews
@Composable
private fun SectionHeaderSkeletonPreview() {
    EpisodiveTheme {
        Column {
            SectionHeader(
                title = "Preview",
                actionIcon = EpisodiveIcons.CaretRight,
                actionIconContentDescription = "See all Preview",
                onActionClick = {},
            )
            SectionHeaderSkeleton(hasAction = true)
        }
    }
}

@DevicePreviews
@Composable
private fun SubSectionHeaderPreview() {
    EpisodiveTheme {
        SubSectionHeader(
            title = "Preview",
        ) {
            Text(
                text = "Content",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun FadeTopBarLayoutPreview() {
    EpisodiveTheme {
        FadeTopBarLayout(
            state = rememberLazyListState(),
            title = "Title",
            onBack = {},
        ) {}
    }
}