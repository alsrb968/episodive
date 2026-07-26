package io.jacob.episodive.feature.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodiveIconText
import io.jacob.episodive.core.designsystem.component.FadeTopBarLayout
import io.jacob.episodive.core.designsystem.component.SkeletonBox
import io.jacob.episodive.core.designsystem.component.SkeletonContainer
import io.jacob.episodive.core.designsystem.component.SkeletonLine
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.isRetryable
import io.jacob.episodive.core.testing.model.channelTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.ui.asUiMessage
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun ChannelRoute(
    modifier: Modifier = Modifier,
    viewModel: ChannelViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPodcastClick: (Long) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ChannelEffect.NavigateBack -> onBackClick()
                is ChannelEffect.NavigateToPodcast -> onPodcastClick(effect.podcastId)
            }
        }
    }

    when (val s = state) {
        is ChannelState.Loading -> ChannelSkeleton(
            onBackClick = { viewModel.sendAction(ChannelAction.ClickBack) },
        )

        is ChannelState.Success -> {
            ChannelScreen(
                modifier = modifier,
                channel = s.channel,
                podcasts = s.podcasts,
                onBackClick = { viewModel.sendAction(ChannelAction.ClickBack) },
                onPodcastClick = { viewModel.sendAction(ChannelAction.ClickPodcast(it)) },
            )
        }

        is ChannelState.Error -> ErrorScreen(
            message = s.error.asUiMessage(),
            // NotFound 는 재시도해도 결과가 같으므로 버튼을 감춘다 — 눌러도 아무 일이
            // 없으면 앱이 고장 난 것처럼 보인다.
            onRetry = if (s.error.isRetryable) {
                { viewModel.sendAction(ChannelAction.Retry) }
            } else {
                null
            },
        )
    }
}

@Composable
internal fun ChannelScreen(
    modifier: Modifier = Modifier,
    channel: Channel,
    podcasts: List<Podcast>,
    onBackClick: () -> Unit,
    onPodcastClick: (Long) -> Unit,
) {
    val lazyGridState = rememberLazyGridState()
    val dimension = LocalDimensionTheme.current

    FadeTopBarLayout(
        modifier = modifier,
        state = lazyGridState,
        offset = 300,
        title = channel.title,
        onBack = onBackClick
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyGridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = ChannelGridHorizontalPadding,
                end = ChannelGridHorizontalPadding,
                // 미니플레이어는 높이 + 아래 마진만큼 떠 있으므로 그만큼 비워야
                // 마지막 카드가 가리지 않는다.
                bottom = dimension.playerBarSpace,
            ),
            horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
            verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
        ) {
            item(span = { GridItemSpan(2) }) {
                // 썸네일을 배경으로 뒤에 깔지 않고 리스트의 첫 항목으로 둔다. 그래야
                // 스크롤할 때 팟캐스트 카드들과 같이 위로 올라간다.
                ChannelHero(
                    modifier = Modifier.fullBleed(ChannelGridHorizontalPadding),
                    channel = channel,
                )
            }

            items(
                items = podcasts,
                key = { it.id }
            ) { podcast ->
                ChannelPodcastCard(
                    podcast = podcast,
                    onClick = { onPodcastClick(podcast.id) },
                )
            }

            item(span = { GridItemSpan(2) }) {
                ChannelFooter(channel = channel)
            }
        }
    }
}

/**
 * [ChannelScreen] 로딩 자리.
 *
 * 로딩이 길어지면 사용자가 빠져나갈 수 있어야 하므로 실제 화면과 같은 [FadeTopBarLayout] 으로
 * 감싸 뒤로가기를 살려둔다. 카드는 히어로 아래 4장뿐이라 [LazyVerticalGrid] 대신 정적
 * Column·Row 로 그린다 — 오프스크린 레이어 안에서 Lazy 그리드를 쓸 이유가 없다.
 */
@Composable
private fun ChannelSkeleton(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    val dimension = LocalDimensionTheme.current

    FadeTopBarLayout(
        modifier = modifier,
        state = rememberLazyGridState(),
        title = "",
        onBack = onBackClick,
    ) {
        SkeletonContainer(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 히어로 자리. 실제 ChannelHero 와 같은 fullBleed 처리로 화면 폭 전체를 채운다.
                SkeletonBox(
                    modifier = Modifier
                        .fullBleed(ChannelGridHorizontalPadding)
                        .fillMaxWidth()
                        .aspectRatio(ChannelHeroAspectRatio),
                    shape = RectangleShape,
                )

                // 헤더 텍스트 자리. 실제로는 히어로 위에 겹쳐 뜨지만, 겹치면 같은 회색조 블록끼리
                // 구분이 안 돼 스켈레톤에서는 히어로 아래에 순서대로 둔다.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = ChannelHeaderTopPadding,
                            bottom = ChannelHeaderBottomPadding,
                            start = ChannelHeaderHorizontalPadding,
                            end = ChannelHeaderHorizontalPadding,
                        ),
                ) {
                    SkeletonLine(
                        style = MaterialTheme.typography.labelMedium,
                        widthFraction = 0.25f,
                    )
                    SkeletonLine(
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        widthFraction = 0.6f,
                    )
                    SkeletonLine(
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        widthFraction = 0.45f,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = ChannelGridHorizontalPadding,
                            end = ChannelGridHorizontalPadding,
                            bottom = dimension.playerBarSpace,
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
                    ) {
                        ChannelPodcastCardSkeleton(modifier = Modifier.weight(1f))
                        ChannelPodcastCardSkeleton(modifier = Modifier.weight(1f))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
                    ) {
                        ChannelPodcastCardSkeleton(modifier = Modifier.weight(1f))
                        ChannelPodcastCardSkeleton(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 그리드의 좌우 contentPadding 을 무시하고 화면 폭 전체로 펴는 modifier.
 *
 * contentPadding 은 항목별로 끌 수 없는데, 히어로만 화면 끝까지 닿아야 한다. 여백만큼
 * 넓게 측정한 뒤 절반씩 왼쪽으로 밀어 배치하고, 그리드에는 원래 폭으로 보고한다.
 */
private fun Modifier.fullBleed(horizontalPadding: Dp) = layout { measurable, constraints ->
    val extra = (horizontalPadding * 2).roundToPx()
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))

    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}

/**
 * 채널 썸네일 + 그 위에 얹는 헤더.
 *
 * 스크림은 화면 배경색만 알파를 달리해 겹친다. 이전의 청색 그라디언트는 썸네일 전체에
 * 파란 색조를 덧입혀 원래 사진 색이 나오지 않았다.
 */
@Composable
private fun ChannelHero(
    modifier: Modifier = Modifier,
    channel: Channel,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = modifier.fillMaxWidth()) {
        StateImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ChannelHeroAspectRatio)
                .align(Alignment.TopCenter),
            imageUrl = channel.image,
            contentDescription = channel.title,
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        0f to backgroundColor.copy(alpha = ChannelHeroScrimTopAlpha),
                        0.45f to backgroundColor.copy(alpha = ChannelHeroScrimMidAlpha),
                        1f to backgroundColor,
                    )
                )
        )

        ChannelHeader(
            // 뒤로가기 버튼은 히어로 위에 겹쳐 뜬다. 그만큼 내려야 채널 제목이 버튼에 가리지 않는다.
            modifier = Modifier.padding(
                top = statusBarPadding + ChannelHeaderTopClearance,
                start = ChannelHeaderHorizontalPadding,
                end = ChannelHeaderHorizontalPadding,
            ),
            channel = channel,
        )
    }
}

@Composable
private fun ChannelHeader(
    modifier: Modifier = Modifier,
    channel: Channel,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ChannelHeaderTopPadding, bottom = ChannelHeaderBottomPadding),
    ) {
        Text(
            text = stringResource(R.string.feature_channel_label),
            style = MaterialTheme.typography.labelMedium,
            color = ChannelLabelColor,
            letterSpacing = ChannelLabelLetterSpacing,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = channel.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        val subtitle = stringResource(R.string.feature_channel_subtitle_format)
            .format(channel.count)

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChannelPodcastCard(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    onClick: () -> Unit,
) {
    // 카드 배경은 커버에서 뽑은 색을 쓴다. 커버가 로드되기 전에는 중립 표면색이다.
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    var backgroundColor by remember(podcast.id) { mutableStateOf(fallbackColor) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor.copy(alpha = ChannelPodcastCardBackgroundAlpha))
            .clickable { onClick() }
            .padding(12.dp),
    ) {
        StateImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium),
            imageUrl = podcast.image,
            contentDescription = podcast.title,
            onDominantColorExtracted = { backgroundColor = it },
        )

        Text(
            modifier = Modifier.padding(top = 9.dp),
            text = podcast.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            // 제목이 한 줄인 카드와 두 줄인 카드의 높이가 달라지지 않도록 항상 두 줄을
            // 차지하게 한다. 그리드는 항목 높이를 맞춰 주지 않아 카드 배경이 들쭉날쭉해진다.
            minLines = ChannelPodcastCardTitleLines,
            maxLines = ChannelPodcastCardTitleLines,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.padding(top = 3.dp),
            text = podcast.ownerName.ifEmpty { podcast.author },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * [ChannelPodcastCard] 로딩 자리.
 *
 * 카드 배경은 커버에서 뽑는 동적인 색이라 스켈레톤 시점엔 알 수 없으므로 칠하지 않는다 —
 * 모양만 클립해 실루엣을 맞춘다. 제목은 실제 카드처럼 minLines=maxLines=2 로 고정돼
 * 있으므로 줄 수가 다르면 전환할 때 카드 높이가 튄다.
 */
@Composable
private fun ChannelPodcastCardSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .padding(12.dp),
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
        )

        SkeletonLine(
            modifier = Modifier.padding(top = 9.dp),
            style = MaterialTheme.typography.labelMedium,
        )

        SkeletonLine(
            style = MaterialTheme.typography.labelMedium,
            widthFraction = 0.7f,
        )

        SkeletonLine(
            modifier = Modifier.padding(top = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            widthFraction = 0.5f,
        )
    }
}

@Composable
private fun ChannelFooter(
    modifier: Modifier = Modifier,
    channel: Channel,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // SectionHeader 는 화면 기본 여백(20dp)을 자체적으로 넣는다. 그리드가 이미
            // 12dp 를 두고 있어 합이 32dp 까지 밀리므로 여기서는 쓰지 않고, 채널 제목·카드
            // 내용과 같은 24dp 선에 맞도록 모자란 12dp 만 더한다.
            .padding(horizontal = ChannelHeaderHorizontalPadding - ChannelGridHorizontalPadding),
    ) {
        Text(
            text = stringResource(R.string.feature_channel_introduction),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = channel.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        )

        val uriHandler = LocalUriHandler.current

        EpisodiveIconText(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri(channel.link) }
                .padding(vertical = 20.dp),
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = EpisodiveIcons.WorldShare,
                    contentDescription = "Website",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.feature_channel_website),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            iconLead = false
        )
    }
}

/** 상단 채널 썸네일 비율 (원본 줄 320) */
private const val ChannelHeroAspectRatio = 1.5f

/** 썸네일 위 스크림 — 화면 배경색을 위에서 아래로 짙게 덮어 글자 대비만 만든다. */
private const val ChannelHeroScrimTopAlpha = 0.15f
private const val ChannelHeroScrimMidAlpha = 0.72f

/** 그리드 좌우 여백 — 카드가 화면을 넓게 쓰도록 화면 기본 여백(20)보다 좁게 잡는다. */
private val ChannelGridHorizontalPadding = 12.dp

/** 커버 추출색을 카드 배경으로 쓸 때의 농도. */
private const val ChannelPodcastCardBackgroundAlpha = 0.55f

/** 카드 제목이 항상 차지하는 줄 수 — 줄 수가 다르면 카드 높이가 어긋난다. */
private const val ChannelPodcastCardTitleLines = 2

/** 헤더 "채널" 라벨 색 및 자간 (원본 줄 324) */
private val ChannelLabelColor = Color(0xFF8FB4D6)
private val ChannelLabelLetterSpacing = 0.06.em

/** 헤더 패딩 — 히어로가 화면 폭 전체라 여백을 직접 준다 (원본 줄 324) */
private val ChannelHeaderTopClearance = 64.dp
private val ChannelHeaderTopPadding = 40.dp
private val ChannelHeaderBottomPadding = 20.dp
private val ChannelHeaderHorizontalPadding = 24.dp

@DevicePreviews
@Composable
private fun ChannelScreenPreview() {
    EpisodiveTheme {
        ChannelScreen(
            channel = channelTestData,
            podcasts = podcastTestDataList,
            onBackClick = {},
            onPodcastClick = {},
        )
    }
}

@DevicePreviews
@Composable
private fun ChannelSkeletonPreview() {
    EpisodiveTheme {
        ChannelSkeleton(onBackClick = {})
    }
}

@DevicePreviews
@Composable
private fun ChannelHeaderPreview() {
    EpisodiveTheme {
        ChannelHeader(
            channel = channelTestData
        )
    }
}

@DevicePreviews
@Composable
private fun ChannelFooterPreview() {
    EpisodiveTheme {
        ChannelFooter(
            channel = channelTestData
        )
    }
}