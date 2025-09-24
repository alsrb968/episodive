package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun EpisodiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.onBackground,
//        ),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun EpisodiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    EpisodiveButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = if (leadingIcon != null) {
            ButtonDefaults.ButtonWithIconContentPadding
        } else {
            ButtonDefaults.ContentPadding
        },
    ) {
        EpisodiveButtonContent(
            text = text,
            leadingIcon = leadingIcon,
        )
    }
}

@Composable
fun EpisodiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
//        colors = ButtonDefaults.outlinedButtonColors(
//            contentColor = MaterialTheme.colorScheme.primary,
//        ),
//        border = BorderStroke(
//            width = EpisodiveButtonDefaults.OutlinedButtonBorderWidth,
//            color = if (enabled) {
//                MaterialTheme.colorScheme.primary
//            } else {
//                MaterialTheme.colorScheme.primary.copy(
//                    alpha = EpisodiveButtonDefaults.DISABLED_OUTLINED_BUTTON_BORDER_ALPHA,
//                )
//            },
//        ),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun EpisodiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    EpisodiveOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = if (leadingIcon != null) {
            ButtonDefaults.ButtonWithIconContentPadding
        } else {
            ButtonDefaults.ContentPadding
        },
    ) {
        EpisodiveButtonContent(
            text = text,
            leadingIcon = leadingIcon,
        )
    }
}

@Composable
fun EpisodiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
//        colors = ButtonDefaults.textButtonColors(
//            contentColor = MaterialTheme.colorScheme.onBackground,
//        ),
        content = content,
    )
}

@Composable
fun EpisodiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    EpisodiveTextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        EpisodiveButtonContent(
            text = text,
            leadingIcon = leadingIcon,
        )
    }
}

@Composable
private fun EpisodiveButtonContent(
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    if (leadingIcon != null) {
        Box(Modifier.sizeIn(maxHeight = ButtonDefaults.IconSize)) {
            leadingIcon()
        }
    }
    Box(
        Modifier
            .padding(
                start = if (leadingIcon != null) {
                    ButtonDefaults.IconSpacing
                } else {
                    0.dp
                },
            ),
    ) {
        text()
    }
}

object EpisodiveButtonDefaults {
    const val DISABLED_OUTLINED_BUTTON_BORDER_ALPHA = 0.12f
    val OutlinedButtonBorderWidth = 1.dp
}

@ThemePreviews
@Composable
fun EpisodiveButtonPreview() {
    EpisodiveTheme {
        EpisodiveButton(onClick = {}, text = { Text("Test button") })
    }
}

@ThemePreviews
@Composable
fun EpisodiveOutlinedButtonPreview() {
    EpisodiveTheme {
        EpisodiveOutlinedButton(onClick = {}, text = { Text("Test button") })
    }
}

@ThemePreviews
@Composable
fun EpisodiveButtonLeadingIconPreview() {
    EpisodiveTheme {
        EpisodiveButton(
            onClick = {},
            text = { Text("Test button") },
            leadingIcon = { Icon(imageVector = EpisodiveIcons.Add, contentDescription = null) },
        )
    }
}