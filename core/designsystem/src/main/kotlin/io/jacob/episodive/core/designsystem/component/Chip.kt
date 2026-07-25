package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun EpisodiveFilterChip(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    pill: Boolean = false,
    label: @Composable () -> Unit,
) {
    val chipShape = if (pill) EpisodiveChipDefaults.PillShape else shape

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled && selected -> MaterialTheme.colorScheme.onSurface.copy(
                alpha = EpisodiveChipDefaults.DISABLED_CHIP_CONTAINER_ALPHA,
            )

            !enabled -> Color.Transparent
            pill && selected -> MaterialTheme.colorScheme.onSurface
            selected -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "chipContainerColor"
    )

    val labelColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = EpisodiveChipDefaults.DISABLED_CHIP_CONTENT_ALPHA,
        )

        pill && selected -> MaterialTheme.colorScheme.surface
        selected -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val labelStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = if (pill && !selected) FontWeight.SemiBold else FontWeight.Bold,
    )

    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = {
            ProvideTextStyle(value = labelStyle) {
                label()
            }
        },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = if (!pill && selected) {
            {
                Icon(
                    imageVector = EpisodiveIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(EpisodiveChipDefaults.CheckIconSize),
                )
            }
        } else {
            null
        },
        shape = chipShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = containerColor,
            labelColor = labelColor,
            iconColor = labelColor,
            disabledContainerColor = containerColor,
            disabledLabelColor = labelColor,
            disabledLeadingIconColor = labelColor,
            selectedLabelColor = labelColor,
            selectedLeadingIconColor = labelColor,
        ),
        border = if (pill && !selected) {
            FilterChipDefaults.filterChipBorder(
                enabled = enabled,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                borderWidth = EpisodiveChipDefaults.ChipBorderWidth,
            )
        } else {
            null
        },
    )
}

@ThemePreviews
@Composable
private fun ChipPreview() {
    EpisodiveTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EpisodiveFilterChip(selected = true, onSelectedChange = {}) {
                Text("selected")
            }
            EpisodiveFilterChip(selected = false, onSelectedChange = {}) {
                Text("unselected")
            }
        }
    }
}

@ThemePreviews
@Composable
private fun PillChipPreview() {
    EpisodiveTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EpisodiveFilterChip(selected = true, onSelectedChange = {}, pill = true) {
                Text("selected")
            }
            EpisodiveFilterChip(selected = false, onSelectedChange = {}, pill = true) {
                Text("unselected")
            }
        }
    }
}

object EpisodiveChipDefaults {
    const val DISABLED_CHIP_CONTAINER_ALPHA = 0.12f
    const val DISABLED_CHIP_CONTENT_ALPHA = 0.38f
    val ChipBorderWidth = 1.dp
    val CheckIconSize = 14.dp
    val PillShape = RoundedCornerShape(20.dp)
}