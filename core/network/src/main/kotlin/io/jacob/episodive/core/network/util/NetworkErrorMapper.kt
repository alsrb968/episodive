package io.jacob.episodive.core.network.util

import io.jacob.episodive.core.model.DataError
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private val SERVER_ERROR_RANGE = 500..599

/**
 * 네트워크 계층의 예외를 화면이 설명할 수 있는 원인으로 접는다.
 *
 * 이 프로젝트의 API 인터페이스는 `Response<T>` 로 감싸지 않고 본문 타입을 그대로 반환한다.
 * 그래서 4xx·5xx 는 `isSuccessful` 로 조용히 오는 것이 아니라 Retrofit 이 [HttpException] 을
 * 던지는 경로로만 나타난다.
 *
 * 취소는 여기서 다루지 않는다 — 호출부가 [kotlinx.coroutines.CancellationException] 을 먼저
 * 걸러 다시 던져야 한다. 취소를 실패로 접으면 코루틴 취소가 전파되지 않는다.
 */
fun Throwable.toDataError(): DataError = when (this) {
    is UnknownHostException, is ConnectException -> DataError.Offline

    // SocketTimeoutException 은 InterruptedIOException 의 하위 타입이다. 둘 다 같은 갈래로
    // 접히므로 순서는 결과에 영향을 주지 않는다.
    is SocketTimeoutException, is InterruptedIOException -> DataError.Timeout

    is HttpException -> when (code()) {
        HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> DataError.Unauthorized
        HTTP_NOT_FOUND -> DataError.NotFound
        in SERVER_ERROR_RANGE -> DataError.Server
        else -> DataError.Unexpected(this)
    }

    else -> DataError.Unexpected(this)
}
