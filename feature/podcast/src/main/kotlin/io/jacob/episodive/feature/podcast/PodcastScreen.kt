package io.jacob.episodive.feature.podcast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodeItem
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.component.EpisodiveGradientBackground
import io.jacob.episodive.core.designsystem.component.FadeTopBarLayout
import io.jacob.episodive.core.designsystem.component.LoadingWheel
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
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
                isFollowed = s.isFollowed,
                onFollowClick = { viewModel.sendAction(PodcastAction.ToggleFollowed) },
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
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onBackClick: () -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val listState = rememberLazyListState()

    FadeTopBarLayout(
        modifier = modifier,
        state = listState,
        offset = 900,
        title = podcast.title,
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PodcastHeader(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    podcast = podcast,
                    isFollowed = isFollowed,
                    onFollowClick = onFollowClick,
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "All episodes (${podcast.episodeCount})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            items(
                count = episodes.size,
                key = { episodes[it].id },
            ) { index ->
                episodes[index].let { episode ->
                    EpisodeItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        episode = episode,
                        onClick = { onEpisodeClick(episode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodcastHeader(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
) {
    EpisodiveGradientBackground {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateImage(
                modifier = Modifier
                    .size(220.dp)
                    .clip(shape = RoundedCornerShape(24.dp)),
                imageUrl = podcast.image,
                contentDescription = podcast.title,
            )

            Text(
                text = podcast.author,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            Text(
                text = podcast.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            EpisodiveButton(
                onClick = onFollowClick,
                shape = RoundedCornerShape(16.dp),
                buttonColors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                ),
                text = { Text(stringResource(if (isFollowed) R.string.feature_podcast_unfollow else R.string.feature_podcast_follow)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFollowed) EpisodiveIcons.PersonRemove else EpisodiveIcons.PersonAdd,
                        contentDescription = null
                    )
                },
            )

            Text(
                text = podcast.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
    EpisodiveTheme {
        PodcastScreen(
            podcast = podcastTestData,
            episodes = episodeTestDataList,
            isFollowed = false,
            onFollowClick = {},
            onEpisodeClick = {},
            onBackClick = {},
            onShowSnackbar = { _, _ -> false }
        )
    }
}