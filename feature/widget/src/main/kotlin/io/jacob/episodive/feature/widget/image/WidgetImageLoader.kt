package io.jacob.episodive.feature.widget.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import timber.log.Timber

object WidgetImageLoader {
    private const val DEFAULT_SIZE_PX = 256

    /**
     * Coil 을 통해 위젯 썸네일 Bitmap 을 로드한다.
     * - `allowHardware(false)` 로 RemoteViews 직렬화 호환 보장
     * - 최대 sizePx 로 제한해 1MB RemoteViews 한도 회피
     */
    suspend fun loadWidgetBitmap(
        context: Context,
        url: String?,
        sizePx: Int = DEFAULT_SIZE_PX,
    ): Bitmap? {
        if (url.isNullOrBlank()) return null
        return runCatching {
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(sizePx)
                .scale(Scale.FILL)
                // EXACT 가 없으면 Coil 기본 INEXACT 가 요청 px 를 무시하고 큰 샘플을 반환해
                // RemoteViews 비트맵 페이로드가 폭증한다(1MB Binder 한도 초과 위험).
                .precision(Precision.EXACT)
                .allowHardware(false)
                .build()
            when (val result = ImageLoader(context).execute(request)) {
                is SuccessResult -> (result.drawable as? BitmapDrawable)?.bitmap
                is ErrorResult -> {
                    Timber.w(result.throwable, "loadWidgetBitmap ERROR url=%s", url)
                    null
                }
            }
        }.getOrElse { e ->
            Timber.e(e, "loadWidgetBitmap threw for url=%s", url)
            null
        }
    }
}
