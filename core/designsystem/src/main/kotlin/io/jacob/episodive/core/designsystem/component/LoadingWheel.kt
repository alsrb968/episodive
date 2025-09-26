package io.jacob.episodive.core.designsystem.component

import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews

@Composable
fun LoadingWheel(
    modifier: Modifier = Modifier,
) {
    ContainedLoadingIndicator(
        modifier = modifier
    )
}

@ThemePreviews
@Composable
private fun LoadingWheelPreview() {
    EpisodiveTheme {
        LoadingWheel()
    }
}