package io.jacob.episodive.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Channel
import io.jacob.episodive.core.testing.model.channelTestData

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