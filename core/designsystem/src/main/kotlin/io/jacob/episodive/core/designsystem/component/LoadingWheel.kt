package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews

@Composable
fun LoadingWheel(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        ContainedLoadingIndicator(
            modifier = Modifier
                .align(Alignment.Center),
        )
    }
}

@DevicePreviews
@Composable
private fun LoadingWheelPreview() {
    EpisodiveTheme {
        LoadingWheel()
    }
}