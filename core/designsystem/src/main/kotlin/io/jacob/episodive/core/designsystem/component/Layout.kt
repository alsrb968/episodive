package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
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
    Scaffold(
        modifier = modifier,
        topBar = {
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
        content(
            paddingValues,
            scrollBehavior.nestedScrollConnection
        )
    }
}

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