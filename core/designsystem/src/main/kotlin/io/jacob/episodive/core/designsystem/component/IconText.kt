package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun EpisodiveIconText(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
        text()
    }
}

@ThemePreviews
@Composable
private fun EpisodiveIconTextPreview() {
    EpisodiveTheme {
        EpisodiveIconText(
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.PersonAdd,
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = "12,3k",
                )
            }
        )
    }
}