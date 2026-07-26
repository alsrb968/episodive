package io.jacob.episodive.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.component.EpisodiveIconText
import io.jacob.episodive.core.designsystem.component.EpisodiveIconToggleButton
import io.jacob.episodive.core.designsystem.component.EpisodiveOutlinedButton
import io.jacob.episodive.core.designsystem.component.HtmlTextContainer
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.mapper.toHumanReadable
import io.jacob.episodive.core.testing.model.podcastTestData

@Stable
@Composable
fun PodcastsSection(
    modifier: Modifier = Modifier,
    title: String,
    podcasts: List<Podcast>,
    subtitleProvider: ((Podcast) -> String)? = null,
    onMore: () -> Unit = {},
    onPodcastClick: (Podcast) -> Unit = {},
) {
    SectionHeader(
        modifier = modifier,
        title = title,
    ) {
        val dimension = LocalDimensionTheme.current
        val lazyListState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(
            lazyListState = lazyListState,
            snapPosition = SnapPosition.Start,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            state = lazyListState,
            flingBehavior = flingBehavior,
            // 같은 화면의 채널·에피소드 섹션은 20dp 화면 패딩을 쓴다. 여기만 16dp 면
            // 한 화면 안에 좌측 정렬선이 두 개 생긴다 (원본 줄 242).
            horizontalArrangement = Arrangement.spacedBy(dimension.carouselSpacing),
            contentPadding = PaddingValues(horizontal = dimension.screenPadding),
            // 짧은 캐러셀은 빠른 스와이프의 드래그 구간만으로도 가장자리 stretch 가 쌓여
            // 릴리즈 시 움찔거림이 생기므로 overscroll 을 사용하지 않는다.
            overscrollEffect = null,
        ) {
            podcasts(
                podcasts = podcasts,
                subtitleProvider = subtitleProvider,
                onPodcastClick = onPodcastClick
            )
        }
    }
}

fun LazyListScope.podcasts(
    itemModifier: Modifier = Modifier,
    podcasts: List<Podcast>,
    subtitleProvider: ((Podcast) -> String)? = null,
    onPodcastClick: (Podcast) -> Unit,
) = items(
    items = podcasts,
    key = { it.id }
) { podcast ->
    PodcastItem(
        modifier = itemModifier,
        podcast = podcast,
        subtitle = subtitleProvider?.invoke(podcast),
        onClick = { onPodcastClick(podcast) }
    )
}

@Composable
fun PodcastItem(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    val textSectionMinHeight = rememberPodcastTextSectionMinHeight()
    val subtitleText = subtitle
        ?: "${podcast.episodeCount} ${stringResource(R.string.core_ui_episodes)}"

    Column(
        modifier = Modifier
            // 기본 폭은 캐러셀 셀(118dp)이다. 그리드처럼 폭을 바깥에서 정하는 호출부가
            // weight/fillMaxWidth 로 덮어쓸 수 있도록 modifier 보다 앞에 둔다.
            .width(LocalDimensionTheme.current.coverCarousel)
            .then(modifier)
            .clickable { onClick() },
    ) {
        StateImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.large),
            imageUrl = podcast.image,
            contentDescription = podcast.title,
        )

        Spacer(modifier = Modifier.height(CoverTitleSpacing))

        Column(
            modifier = Modifier.heightIn(min = textSectionMinHeight),
        ) {
            Text(
                text = podcast.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(TextSectionSpacing))

            Text(
                text = subtitleText,
                // 원본의 11~12px 메타는 굵기 지정이 없다(=400). labelSmall 은 700 이라
                // 보조 정보가 제목만큼 굵어져 위계가 뭉개진다 (원본 줄 199·243).
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val CoverTitleSpacing = 9.dp
private val TextSectionSpacing = 4.dp

@Composable
private fun rememberPodcastTextSectionMinHeight(): Dp {
    val density = LocalDensity.current
    val typography = MaterialTheme.typography
    return remember(density, typography) {
        with(density) {
            typography.labelMedium.lineHeight.toDp() +
                TextSectionSpacing +
                typography.bodySmall.lineHeight.toDp()
        }
    }
}

private val DetailItemCoverSize = 96.dp

@Composable
fun PodcastDetailItem(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    isFollowed: Boolean = podcast.isFollowed,
    onClick: () -> Unit = {},
    onToggleFollowed: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick),
    ) {
        StateImage(
            modifier = Modifier
                .size(DetailItemCoverSize)
                .clip(EpisodiveShapes.coverForSize(96)),
            imageUrl = podcast.image,
            contentDescription = podcast.title,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = podcast.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                EpisodiveIconToggleButton(
                    checked = isFollowed,
                    onCheckedChange = { onToggleFollowed() },
                    icon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = EpisodiveIcons.PersonAdd,
                            contentDescription = podcast.title,
                        )
                    },
                    checkedIcon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = EpisodiveIcons.PersonRemove,
                            contentDescription = podcast.title,
                        )
                    },
                )
            }

            FlowRow(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                podcast.ownerName.ifEmpty { podcast.author }.let { owner ->
                    if (owner.isNotEmpty()) {
                        EpisodiveIconText(
                            icon = {
                                Icon(
                                    imageVector = EpisodiveIcons.Owner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp),
                                )
                            },
                            text = {
                                Text(
                                    text = podcast.ownerName.ifEmpty { podcast.author },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        )
                    }
                }

                EpisodiveIconText(
                    icon = {
                        Icon(
                            imageVector = EpisodiveIcons.PublicationDate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                    },
                    text = {
                        Text(
                            text = (podcast.newestItemPublishTime
                                ?: podcast.lastUpdateTime).toHumanReadable(),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                )

                EpisodiveIconText(
                    icon = {
                        Icon(
                            imageVector = EpisodiveIcons.ListNumbered,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                    },
                    text = {
                        Text(
                            text = "${podcast.episodeCount} ${stringResource(R.string.core_ui_episodes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HtmlTextContainer(
                text = podcast.description,
            ) {
                Text(
                    text = it,
                    maxLines = 4,
                    minLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun PodcastSimpleItem(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    onClick: () -> Unit,
    onToggleFollowed: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StateImage(
            modifier = Modifier
                .size(50.dp)
                .clip(EpisodiveShapes.field),
            imageUrl = podcast.image,
            contentDescription = podcast.title,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = podcast.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = podcast.ownerName.ifEmpty { podcast.author },
                // 저자명도 메타라 굵기 500 이 맞다 (원본 줄 326).
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        EpisodiveOutlinedButton(
            onClick = onToggleFollowed,
            shape = EpisodiveShapes.miniPlayer,
            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 7.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (podcast.isFollowed) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
        ) {
            Text(
                text = stringResource(
                    if (podcast.isFollowed) R.string.core_ui_unfollow
                    else R.string.core_ui_follow
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun PodcastItemPreview() {
    EpisodiveTheme {
        PodcastItem(
            podcast = podcastTestData,
        )
    }
}

@DevicePreviews
@Composable
private fun PodcastWithAuthorPreview() {
    EpisodiveTheme {
        PodcastItem(
            podcast = podcastTestData,
            subtitle = podcastTestData.ownerName.ifEmpty { podcastTestData.author },
        )
    }
}

@DevicePreviews
@Composable
private fun PodcastDetailItemPreview() {
    EpisodiveTheme {
        PodcastDetailItem(
            podcast = podcastTestData,
        )
    }
}

@DevicePreviews
@Composable
private fun PodcastSimpleItemPreview() {
    EpisodiveTheme {
        PodcastSimpleItem(
            podcast = podcastTestData,
            onClick = {},
            onToggleFollowed = {},
        )
    }
}