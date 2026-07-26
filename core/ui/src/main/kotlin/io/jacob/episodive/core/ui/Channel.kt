package io.jacob.episodive.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.SectionHeaderSkeleton
import io.jacob.episodive.core.designsystem.component.SkeletonBox
import io.jacob.episodive.core.designsystem.component.SkeletonLine
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.testing.model.channelTestData
import io.jacob.episodive.core.testing.model.channelTestDataList

@Composable
fun ChannelSection(
    modifier: Modifier = Modifier,
    title: String,
    channels: List<Channel>,
    onChannelClick: (Long) -> Unit,
) {
    SectionHeader(
        modifier = modifier,
        title = title,
    ) {
        val lazyListState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(
            lazyListState = lazyListState,
            snapPosition = SnapPosition.Start,
        )

        val dimension = LocalDimensionTheme.current

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
            flingBehavior = flingBehavior,
            horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
            // 짧은 캐러셀은 빠른 스와이프의 드래그 구간만으로도 가장자리 stretch 가 쌓여
            // 릴리즈 시 움찔거림이 생기므로 overscroll 을 사용하지 않는다.
            overscrollEffect = null,
        ) {
            items(
                items = channels,
                key = { it.id }
            ) { channel ->
                ChannelItem(
                    channel = channel,
                    onClick = { onChannelClick(channel.id) }
                )
            }
        }
    }
}

/** [ChannelSection] 로딩 자리. 헤더와 카드 개수만큼의 캐러셀을 그대로 흉내낸다. */
@Composable
fun ChannelSectionSkeleton(
    modifier: Modifier = Modifier,
    count: Int = 3,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        SectionHeaderSkeleton()

        val dimension = LocalDimensionTheme.current

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimension.gridSpacing),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
            // 일반 Row 는 화면 밖으로 넘치는 마지막 카드 폭이 0으로 찌그러진다. LazyRow 로
            // 실제 폭을 유지한 채 잘려 보이게 해야 "옆으로 더 있다"가 로딩 중에도 읽힌다.
            userScrollEnabled = false,
        ) {
            items(count) {
                ChannelItemSkeleton()
            }
        }
    }
}

/** 채널 카드 폭과 설명 박스 높이. */
private val ChannelItemWidth = 250.dp
private val ChannelItemCaptionHeight = 80.dp

@Composable
fun ChannelItem(
    modifier: Modifier = Modifier,
    channel: Channel,
    onClick: () -> Unit,
) {
    // 카드 배경은 채널 아트에서 뽑은 색을 쓴다. 커버가 로드되기 전에는 카드 표면색이다.
    val fallbackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    var backgroundColor by remember(channel.id) { mutableStateOf(fallbackColor) }

    Column(
        modifier = modifier
            .width(ChannelItemWidth)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() },
    ) {
        StateImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            imageUrl = channel.image,
            contentDescription = channel.title,
            onDominantColorExtracted = { backgroundColor = it },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChannelItemCaptionHeight)
                .background(color = backgroundColor.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = channel.description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * [ChannelItem] 로딩 자리. 커버와 캡션 각각에 모양을 주지 않고 카드 전체를 통째로
 * extraLarge 로 클립한다 — 둘을 따로 클립하면 이미지·캡션이 맞닿는 경계에 실제 카드에는
 * 없는 라운딩이 생겨버린다.
 */
@Composable
fun ChannelItemSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(ChannelItemWidth)
            .clip(MaterialTheme.shapes.extraLarge),
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RectangleShape,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChannelItemCaptionHeight)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SkeletonLine(style = MaterialTheme.typography.labelMedium)
                SkeletonLine(style = MaterialTheme.typography.labelMedium, widthFraction = 0.7f)
                SkeletonLine(style = MaterialTheme.typography.labelMedium, widthFraction = 0.4f)
            }
        }
    }
}

@DevicePreviews
@Composable
private fun ChannelItemPreview() {
    EpisodiveTheme {
        ChannelItem(
            channel = channelTestData,
            onClick = {}
        )
    }
}

@ThemePreviews
@Composable
private fun ChannelItemSkeletonPreview() {
    EpisodiveTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChannelItem(
                channel = channelTestData,
                onClick = {}
            )
            ChannelItemSkeleton()
        }
    }
}

@ThemePreviews
@Composable
private fun ChannelSectionSkeletonPreview() {
    EpisodiveTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ChannelSection(
                title = "Preview",
                channels = channelTestDataList.take(3),
                onChannelClick = {},
            )
            ChannelSectionSkeleton()
        }
    }
}