package io.jacob.episodive.core.ui.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.share.ShareContent
import io.jacob.episodive.core.model.share.ShareLabels
import io.jacob.episodive.core.model.share.toClipShareContent
import io.jacob.episodive.core.model.share.toShareContent
import io.jacob.episodive.core.ui.R

/**
 * 공유 시트를 띄우는 단일 창구.
 *
 * ViewModel 을 거치지 않는다. 공유는 ViewModel 이 아는 상태를 바꾸지도, 그 상태에 기대지도
 * 않는 — 화면이 이미 손에 쥔 도메인 객체만으로 끝나는 UI 사이드이펙트다. Effect 로 올리면
 * `EpisodeClipItem` 같은 공용 컴포넌트가 각 feature ViewModel 로 콜백을 여러 단 역주입해야
 * 하는데, 그 대가로 얻는 것이 없다. 문구 조립의 정확성은 `:core:model` 의 순수 함수가 진다.
 *
 * 대신 `Intent` 를 손으로 짓는 자리는 여기 하나로 모은다 — 흩어지면 `EXTRA_SUBJECT` 누락이나
 * `ActivityNotFoundException` 미처리 같은 것이 화면마다 따로 생긴다.
 */
@Stable
class ShareLauncher internal constructor(
    private val context: Context,
    private val labels: ShareLabels,
    private val onError: (Throwable) -> Unit,
) {
    fun share(podcast: Podcast) = launch(podcast.toShareContent(labels))

    /**
     * [positionMs] 를 주면 듣고 있던 지점을 함께 싣는다(너무 이른 지점은 모델이 걸러낸다).
     * [podcast] 는 에피소드에 웹 링크가 없을 때의 폴백이라, 손에 쥔 화면이면 넘겨 주는 편이 낫다.
     */
    fun share(episode: Episode, podcast: Podcast? = null, positionMs: Long? = null) =
        launch(episode.toShareContent(labels, podcast, positionMs))

    fun shareClip(episode: Episode, podcast: Podcast? = null) =
        launch(episode.toClipShareContent(labels, podcast))

    private fun launch(content: ShareContent) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TEXT
            putExtra(Intent.EXTRA_SUBJECT, content.subject)
            putExtra(Intent.EXTRA_TEXT, content.text)
        }

        try {
            // chooser 제목은 넘기지 않는다 — API 29 부터 시스템이 무시한다.
            context.startActivity(Intent.createChooser(intent, null))
        } catch (e: ActivityNotFoundException) {
            // 공유를 받을 앱이 하나도 없는 기기(순정 에뮬레이터 등)에서 실제로 난다.
            onError(e)
        }
    }

    private companion object {
        const val MIME_TEXT = "text/plain"
    }
}

/**
 * [onError] 는 화면이 이미 들고 있는 `onShowSnackbar` 로 이으면 된다.
 */
@Composable
fun rememberShareLauncher(onError: (Throwable) -> Unit = {}): ShareLauncher {
    val context = LocalContext.current
    val currentOnError by rememberUpdatedState(onError)

    val labels = ShareLabels(
        episodeSubjectFormat = stringResource(R.string.core_ui_share_subject_episode_format),
        clipLineFormat = stringResource(R.string.core_ui_share_line_clip_format),
        positionLineFormat = stringResource(R.string.core_ui_share_line_position_format),
        openInAppFormat = stringResource(R.string.core_ui_share_line_open_in_app_format),
    )

    // labels 는 data class 라 로케일이 바뀌어야만 키가 달라진다.
    return remember(context, labels) {
        ShareLauncher(
            context = context,
            labels = labels,
            onError = { currentOnError(it) },
        )
    }
}
