package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.mapper.toHumanReadable

@Composable
fun EpisodesSection(
    modifier: Modifier = Modifier,
    title: String,
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
) {
    SectionHeader(
        modifier = modifier,
        title = title,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            episodes.forEach { episode ->
                EpisodeItem(
                    episode = episode,
                    onClick = { onEpisodeClick(episode) }
                )
            }
        }
    }
}

@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StateImage(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp)),
            imageUrl = episode.image.ifEmpty { episode.feedImage },
            contentDescription = episode.title,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            val subTitle = "%s • %s".format(
                episode.datePublished.toHumanReadable(),
                episode.duration?.toHumanReadable() ?: episode.feedTitle
            ).trim()

            Text(
                text = subTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = EpisodiveIcons.PlayArrow,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Play episode",
            )
        }
    }
}
