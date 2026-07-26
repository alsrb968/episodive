package io.jacob.episodive.core.designsystem.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.R
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews

/**
 * 화면 전체가 실패했을 때의 자리.
 *
 * [onRetry] 를 주지 않으면 버튼 없이 문구만 그린다. 다시 시도해도 같은 답이 오는 실패
 * (없는 대상, 인증 실패)에서는 버튼을 두지 않는 편이 낫다 — 눌러도 아무 일이 없으면 앱이
 * 고장 난 것처럼 보인다.
 */
@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ErrorScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Column(
                modifier = Modifier.padding(top = ErrorScreenActionSpacing),
            ) {
                EpisodiveButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.core_designsystem_retry))
                }
            }
        }
    }
}

private val ErrorScreenHorizontalPadding = 32.dp
private val ErrorScreenActionSpacing = 20.dp

@DevicePreviews
@Composable
private fun ErrorScreenPreview() {
    EpisodiveTheme {
        ErrorScreen(
            message = "연결이 끊겼어요.\n네트워크를 확인하고 다시 시도해 주세요.",
            onRetry = {},
        )
    }
}

@DevicePreviews
@Composable
private fun ErrorScreenWithoutRetryPreview() {
    EpisodiveTheme {
        ErrorScreen(message = "요청한 팟캐스트를 찾을 수 없어요.")
    }
}
