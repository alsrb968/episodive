package io.jacob.episodive.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.component.EpisodiveDragHandle
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.opml.OpmlImportProgress

/**
 * 팔로우 팟캐스트를 OPML 로 내보내거나, 다른 앱의 OPML 을 가져와 팔로우에 더하는 시트.
 * [progress] 가 있는 동안(진행 중이거나 막 끝났을 때)에는 버튼 대신 그 결과를 그린다.
 */
@Composable
internal fun OpmlSheet(
    modifier: Modifier = Modifier,
    progress: OpmlImportProgress?,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dimension = LocalDimensionTheme.current
    val isRunning = progress != null && !progress.isFinished

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = EpisodiveShapes.bottomSheet,
        dragHandle = { EpisodiveDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimension.screenPadding)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_library_opml_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EpisodiveButton(
                    modifier = Modifier.weight(1f),
                    onClick = onImport,
                    enabled = !isRunning,
                    text = { Text(stringResource(R.string.feature_library_opml_import)) },
                    leadingIcon = { Icon(EpisodiveIcons.Import, contentDescription = null) },
                )

                EpisodiveButton(
                    modifier = Modifier.weight(1f),
                    onClick = onExport,
                    enabled = !isRunning,
                    text = { Text(stringResource(R.string.feature_library_opml_export)) },
                    leadingIcon = { Icon(EpisodiveIcons.Export, contentDescription = null) },
                )
            }

            if (progress != null) {
                OpmlProgressContent(progress = progress)
            }
        }
    }
}

@Composable
private fun OpmlProgressContent(modifier: Modifier = Modifier, progress: OpmlImportProgress) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!progress.isFinished) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                gapSize = (-4).dp,
                drawStopIndicator = {},
                // 비율 계산은 모델이 한 곳에서 한다. 여기서 다시 계산하면 정의가 두 벌이 된다.
                progress = { progress.progress },
            )

            Text(
                text = stringResource(
                    R.string.feature_library_opml_import_progress,
                    progress.done,
                    progress.total,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 시트를 닫아도 작업이 계속된다는 것을 알려야, 사용자가 닫고 나서 결과가
            // 궁금해 다시 열었을 때 "멈춘 게 아니었구나" 를 새로 알 필요가 없다.
            Text(
                text = stringResource(R.string.feature_library_opml_import_running_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // 요약은 오프라인으로 멈췄을 때도 그린다. 그전에 이미 더해진 것이 있는데
            // 원인만 말하고 성과를 감추면, 사용자는 다시 시도할 때 처음부터인 줄 안다.
            Text(
                text = stringResource(
                    R.string.feature_library_opml_import_result,
                    progress.added,
                    progress.alreadyFollowed,
                    progress.notFound,
                    progress.failed.size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (progress.stoppedOffline) {
                // 네트워크가 없어 멈춘 것은 개별 항목이 아니라 남은 전부가 같은 이유로
                // 실패할 것이라 끊은 것이다. 원인을 그대로 말해야 사용자가 파일을
                // 의심하지 않는다.
                Text(
                    text = stringResource(R.string.feature_library_opml_import_offline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@DevicePreviews
@Composable
private fun OpmlSheetPreview() {
    EpisodiveTheme {
        OpmlSheet(
            progress = null,
            onImport = {},
            onExport = {},
            onDismiss = {},
        )
    }
}

@DevicePreviews
@Composable
private fun OpmlSheetRunningPreview() {
    EpisodiveTheme {
        OpmlSheet(
            progress = OpmlImportProgress(total = 20, done = 8, added = 5, alreadyFollowed = 3),
            onImport = {},
            onExport = {},
            onDismiss = {},
        )
    }
}
