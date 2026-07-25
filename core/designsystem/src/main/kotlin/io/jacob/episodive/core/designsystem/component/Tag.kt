package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun EpisodiveTopicTag(
    modifier: Modifier = Modifier,
    followed: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        val containerColor = if (followed) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = EpisodiveTagDefaults.UNFOLLOWED_TOPIC_TAG_CONTAINER_ALPHA,
            )
        }
        val contentColor = if (followed) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        TextButton(
            onClick = onClick,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = EpisodiveTagDefaults.ContentPadding,
            colors = ButtonDefaults.textButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = EpisodiveTagDefaults.DISABLED_TOPIC_TAG_CONTAINER_ALPHA,
                ),
            ),
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            ) {
                text()
            }
        }
    }
}

@ThemePreviews
@Composable
private fun TagPreview() {
    EpisodiveTheme {
        EpisodiveTopicTag(followed = true, onClick = {}) {
            Text("Topic".uppercase())
        }
    }
}

object EpisodiveTagDefaults {
    const val UNFOLLOWED_TOPIC_TAG_CONTAINER_ALPHA = 0.5f
    const val DISABLED_TOPIC_TAG_CONTAINER_ALPHA = 0.12f
    val ContentPadding = PaddingValues(horizontal = 13.dp, vertical = 7.dp)
}