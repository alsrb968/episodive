package io.jacob.episodive.core.data.util

import coil.intercept.Interceptor
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.SuccessResult
import timber.log.Timber
import java.io.IOException

/**
 * 이미지 요청이 엔진에 닿기 전에 세 가지를 처리한다.
 *
 * 1. 빈 URL을 걷어낸다 — 실패가 확정된 왕복을 아낀다.
 * 2. 경로의 연속 슬래시를 접는다 — 서버에 따라 404가 되는 것을 살린다.
 * 3. 최근 실패한 URL은 TTL 동안 네트워크를 잠근다.
 *
 * **OkHttp 인터셉터가 아니라 Coil 인터셉터인 이유**: 정규화를 OkHttp 단에서 하면 Coil의 캐시 키는
 * 고치기 전 문자열로 계산되어, 같은 이미지가 두 번 캐싱되고 실패 기록도 두 갈래로 갈린다. 여기서
 * `data` 를 바꾸면 캐시 키·실패 기록·실제 요청 URL이 한 번에 정렬된다.
 */
class ImageRequestInterceptor(
    private val failureCache: ImageFailureCache,
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val data = request.data

        // 문자열이 아닌 데이터(리소스 ID, Bitmap, File 등)는 이 인터셉터가 다룰 것이 없다.
        if (data !is String) return chain.proceed(request)

        if (data.isBlank()) {
            return ErrorResult(request.error, request, BlankImageUrlException)
        }

        val normalized = normalizeImageUrl(data)
        val blocked = failureCache.isBlocked(normalized)

        val next = if (normalized == data && !blocked) {
            request
        } else {
            request.newBuilder()
                .data(normalized)
                // 차단은 "네트워크를 타지 않는다"이지 "캐시된 그림도 안 쓴다"가 아니다. 요청을
                // 여기서 끊어버리면 메모리·디스크 캐시 조회(EngineInterceptor 안에 있다)까지
                // 건너뛰어, 방금까지 멀쩡히 보이던 커버가 TTL 동안 플레이스홀더로 바뀐다.
                .apply { if (blocked) networkCachePolicy(CachePolicy.DISABLED) }
                .build()
        }

        return chain.proceed(next).also { result ->
            when (result) {
                // 차단 중에 성공했다면 캐시가 받아준 것이지 서버가 고쳐진 것이 아니다.
                // 그때 기록을 지우면 다음 요청이 다시 네트워크로 나가 같은 실패를 반복한다.
                is SuccessResult -> if (!blocked) failureCache.clear(normalized)

                is ErrorResult -> if (!blocked) {
                    result.throwable.toImageFailureTtl()?.let { ttl ->
                        failureCache.record(normalized, ttl.inWholeMilliseconds)
                        // 로깅은 여기 한 곳에서만 한다. 차단된 요청은 이 분기로 오지 않으므로
                        // 같은 URL이 로그를 도배하지 않고 자연스럽게 1회로 수렴한다.
                        Timber.w("Image load failed ($ttl 동안 네트워크 잠금): $normalized — ${result.throwable}")
                    }
                }
            }
        }
    }
}

/** URL이 비어 네트워크를 타지 않았다. */
object BlankImageUrlException : IOException("이미지 URL이 비어 있다")
