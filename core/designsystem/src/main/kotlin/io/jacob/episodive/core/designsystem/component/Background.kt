package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.GradientColors
import io.jacob.episodive.core.designsystem.theme.LocalBackgroundTheme
import io.jacob.episodive.core.designsystem.theme.LocalGradientColors
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

/**
 * The main background for the app.
 * Uses [LocalBackgroundTheme] to set the color and tonal elevation of a [Surface].
 *
 * @param modifier Modifier to be applied to the background.
 * @param content The background content.
 */
@Composable
fun EpisodiveBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val color = LocalBackgroundTheme.current.color
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    Surface(
        color = if (color == Color.Unspecified) Color.Transparent else color,
        tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}

/**
 * A gradient background for select screens. Uses [LocalBackgroundTheme] to set the gradient colors
 * of a [Box] within a [Surface].
 *
 * @param modifier Modifier to be applied to the background.
 * @param gradientColors The gradient colors to be rendered.
 * @param content The background content.
 */
@Composable
fun EpisodiveGradientBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = LocalGradientColors.current,
    content: @Composable () -> Unit,
) {
    val currentTopColor by rememberUpdatedState(gradientColors.top)
    Box(
        modifier = modifier
            .background(
                color = gradientColors.container
            )
            .drawWithCache {
                // v2: 화면 상단만 물들이고 82% 지점부터 투명하게 사라진다.
                val topGradient = Brush.verticalGradient(
                    0f to if (currentTopColor == Color.Unspecified) {
                        Color.Transparent
                    } else {
                        currentTopColor
                    },
                    0.82f to Color.Transparent,
                    startY = 0f,
                    endY = size.height,
                )

                onDrawBehind {
                    drawRect(topGradient)
                }
            },
    ) {
        content()
    }
}

@ThemePreviews
@Composable
private fun EpisodiveBackgroundPreview() {
    EpisodiveTheme {
        EpisodiveBackground(Modifier.size(100.dp), content = {})
    }
}

@ThemePreviews
@Composable
private fun EpisodiveGradientBackgroundPreview() {
    EpisodiveTheme {
        EpisodiveGradientBackground(Modifier.size(100.dp), content = {})
    }
}