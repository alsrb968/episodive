package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
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

@Composable
fun EpisodiveScaffold(
    modifier: Modifier = Modifier,
    title: String,
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
                            text = title,
                            // 화면 제목은 34/800/-.03em (원본 줄 232, 485). headlineMedium(28)은
                            // 한 단계 작아서 v2 의 오버사이즈 타이포 대비가 죽는다.
                            style = MaterialTheme.typography.displaySmall,
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

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                IconButton(
                    onClick = onActionClick
                ) {
                    Icon(
                        imageVector = actionIcon,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = actionIconContentDescription
                    )
                }
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
 */
@Composable
fun SectionHeaderSkeleton(
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    titleWidthFraction: Float = SectionHeaderSkeletonTitleWidth,
) {
    val dimension = LocalDimensionTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        SkeletonLine(
            modifier = Modifier.padding(horizontal = dimension.screenPadding),
            style = titleStyle,
            widthFraction = titleWidthFraction,
        )

        Spacer(modifier = Modifier.height(SectionHeaderContentSpacing))
    }
}

private val SectionHeaderContentSpacing = 14.dp
private const val SectionHeaderSkeletonTitleWidth = 0.4f

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