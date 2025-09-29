package io.jacob.episodive.feature.podcast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodeItem
import io.jacob.episodive.core.designsystem.component.FadeTopBarLayout
import io.jacob.episodive.core.designsystem.component.LoadingWheel
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.jacob.episodive.core.testing.model.podcastTestData

@Composable
internal fun PodcastRoute(
    modifier: Modifier = Modifier,
    viewModel: PodcastViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is PodcastState.Loading -> LoadingScreen()

        is PodcastState.Success -> {
            PodcastScreen(
                modifier = modifier,
                podcast = s.podcast,
                episodes = s.episodes,
                onEpisodeClick = { episode ->
                    // TODO
                },
                onBackClick = onBackClick,
                onShowSnackbar = onShowSnackbar
            )
        }

        is PodcastState.Error -> ErrorScreen(message = s.message)
    }
}

@Composable
private fun PodcastScreen(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val listState = rememberLazyListState()

    FadeTopBarLayout(
        modifier = modifier,
        state = listState,
        title = podcast.title,
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StateImage(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(shape = RoundedCornerShape(8.dp)),
                        imageUrl = podcast.image,
                        contentDescription = podcast.title,
                    )

                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp),
                        text = podcast.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                ),
                            onClick = { /* TODO */ }
                        ) {
                            Icon(
                                imageVector = EpisodiveIcons.FileDownload,
                                contentDescription = null
                            )
                        }

                        IconButton(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                ),
                            onClick = {
//                                onEpisodeClick(episodes)
                            }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(48.dp),
                                imageVector = EpisodiveIcons.PlayArrow,
                                contentDescription = null
                            )
                        }

                        IconButton(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                ),
                            onClick = { /* TODO */ }
                        ) {
                            Icon(
                                imageVector = EpisodiveIcons.Share,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            items(
                count = episodes.size,
                key = { episodes[it].id },
            ) { index ->
                episodes[index].let { episode ->
                    EpisodeItem(
                        episode = episode,
                        onClick = { onEpisodeClick(episode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingWheel()
    }
}

@Composable
private fun ErrorScreen(
    modifier: Modifier = Modifier,
    message: String,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message)
    }
}

@DevicePreviews
@Composable
private fun PodcastScreenPreview() {
    PodcastScreen(
        podcast = podcastTestData,
        episodes = episodeTestDataList,
        onEpisodeClick = {},
        onBackClick = {},
        onShowSnackbar = { _, _ -> false }
    )
}