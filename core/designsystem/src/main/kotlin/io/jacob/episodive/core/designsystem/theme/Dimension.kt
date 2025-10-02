package io.jacob.episodive.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class DimensionTheme(
    val playerBarHeight: Dp = Dp.Unspecified,
)

val LocalDimensionTheme = staticCompositionLocalOf { DimensionTheme() }