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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.screen.LoadingScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.testing.model.channelTestData
import io.jacob.episodive.core.testing.model.podcastTestDataList
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
        is ChannelState.Loading -> LoadingScreen()

        is ChannelState.Success -> {
            ChannelScreen(
                modifier = modifier,
                channel = s.channel,
                podcasts = s.podcasts,
                onBackClick = { viewModel.sendAction(ChannelAction.ClickBack) },
                onPodcastClick = { viewModel.sendAction(ChannelAction.ClickPodcast(it)) },
            )
        }

        is ChannelState.Error -> ErrorScreen(message = s.message)
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
            maxLines = 2,
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

@Composable
private fun ChannelFooter(
    modifier: Modifier = Modifier,
    channel: Channel,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        SectionHeader(
            title = stringResource(R.string.feature_channel_introduction),
            contentPadding = PaddingValues(0.dp)
        ) {
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