package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun EpisodiveIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        disabledContainerColor = if (checked) {
            MaterialTheme.colorScheme.onBackground.copy(
                alpha = EpisodiveIconButtonDefaults.DISABLED_ICON_BUTTON_CONTAINER_ALPHA,
            )
        } else {
            Color.Transparent
        },
    ),
    icon: @Composable () -> Unit,
    checkedIcon: @Composable () -> Unit = icon,
) {
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
    ) {
        if (checked) checkedIcon() else icon()
    }
}

@Composable
fun EpisodiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(
            alpha = EpisodiveIconButtonDefaults.DISABLED_ICON_BUTTON_CONTAINER_ALPHA,
        ),
    ),
    icon: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
    ) {
        icon()
    }
}

@ThemePreviews
@Composable
private fun EpisodiveIconToggleButtonPreview() {
    EpisodiveTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EpisodiveIconToggleButton(
                checked = true,
                onCheckedChange = { },
                icon = {
                    Icon(
                        imageVector = EpisodiveIcons.Add,
                        contentDescription = null,
                    )
                },
                checkedIcon = {
                    Icon(
                        imageVector = EpisodiveIcons.Check,
                        contentDescription = null,
                    )
                },
            )
            EpisodiveIconToggleButton(
                checked = false,
                onCheckedChange = { },
                icon = {
                    Icon(
                        imageVector = EpisodiveIcons.Add,
                        contentDescription = null,
                    )
                },
                checkedIcon = {
                    Icon(
                        imageVector = EpisodiveIcons.Check,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun EpisodiveIconButtonPreview() {
    EpisodiveTheme {
        EpisodiveIconButton(
            onClick = { },
            icon = {
                Icon(
                    imageVector = EpisodiveIcons.Add,
                    contentDescription = null,
                )
            }
        )
    }
}

object EpisodiveIconButtonDefaults {
    const val DISABLED_ICON_BUTTON_CONTAINER_ALPHA = 0.12f
}