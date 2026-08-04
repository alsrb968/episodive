package io.jacob.episodive.core.data.util

import coil.network.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 이 실패를 기억해 둘 가치가 있는지, 있다면 얼마나인지 판정한다. null이면 기록하지 않는다.
 *
 * 기준은 `DataError.isRetryable` 과 같다 — **다시 시도하면 결과가 달라질 수 있는가.** 달라질 수
 * 있는 실패(끊긴 네트워크, 타임아웃)를 기록하면 지하철에서 스크롤 한 번 한 것만으로 화면의 모든
 * 커버가 블랙리스트에 올라간다. 그게 이 파일이 막아야 할 최악의 시나리오다.
 *
 * 모르는 실패는 기록하지 않는다. 놓친 재요청은 트래픽을 조금 쓰는 것으로 끝나지만, 잘못 기록한
 * 항목은 멀쩡한 커버를 TTL 내내 가린다.
 */
fun Throwable.toImageFailureTtl(): Duration? = when (this) {
    // 네트워크가 없거나 불안정한 것 — 앱 밖 사정이고 곧 바뀐다.
    is UnknownHostException,
    is ConnectException,
    is InterruptedIOException, // SocketTimeoutException 포함
        -> null

    // cleartext 차단. 앱 설정이 바뀌기 전엔 절대 변하지 않는다.
    is UnknownServiceException -> PERMANENT_TTL

    // SSLHandshakeException 은 인증서 문제 전용이 아니다. 불안정한 망에서 핸드셰이크 도중
    // 연결이 끊겨도 같은 예외가 온다("Connection reset by peer"). 그것까지 인증서로 보고
    // 기록하면 지하철에서 스크롤 한 번에 화면의 커버가 전부 몇 시간씩 차단된다.
    // OkHttp 의 RetryAndFollowUpInterceptor.isRecoverable() 과 같은 기준으로 갈라낸다 —
    // 원인이 CertificateException 일 때만 진짜 인증서 문제다.
    is SSLHandshakeException -> if (cause is CertificateException) CERTIFICATE_TTL else null

    // 도메인 불일치는 전송 실패로 생기지 않는다.
    is SSLPeerUnverifiedException,
    is CertificateException,
        -> CERTIFICATE_TTL

    is HttpException -> when (response.code) {
        // 504는 서버가 준 답이 아니라 OkHttp 가 만든 합성 응답인 경우가 있다.
        // 오프라인에서 캐시에 없는 이미지를 요청하면 "Unsatisfiable Request (only-if-cached)"
        // 로 이 코드가 온다(실기기 확인). 이걸 서버 오류로 기록하면 비행기 모드로 스크롤 한 번
        // 한 것만으로 화면의 커버가 전부 차단되고, 네트워크가 돌아와도 TTL 동안 안 뜬다.
        // 진짜 게이트웨이 타임아웃도 일시적이라 기록하지 않아 잃을 것이 없다.
        HTTP_GATEWAY_TIMEOUT -> null
        // 408도 4xx 지만 "다시 물어봐도 같은 답"의 정반대다. 부하 걸린 오리진이 한 번 뱉은
        // 타임아웃 때문에 커버를 하루 종일 막을 이유가 없다.
        HTTP_REQUEST_TIMEOUT -> null
        // 과부하·레이트리밋은 곧 풀린다. 스크롤 한 번에 몰리는 재요청만 막으면 된다.
        HTTP_TOO_MANY_REQUESTS -> TRANSIENT_TTL
        in SERVER_ERROR_RANGE -> TRANSIENT_TTL
        // 없는 파일, 핫링크 차단 — 다시 물어봐도 같은 답이 온다.
        in CLIENT_ERROR_RANGE -> PERMANENT_TTL
        else -> null
    }

    else -> null
}

private val PERMANENT_TTL = 24.hours
private val CERTIFICATE_TTL = 6.hours
private val TRANSIENT_TTL = 5.minutes

private const val HTTP_REQUEST_TIMEOUT = 408
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_GATEWAY_TIMEOUT = 504
private val CLIENT_ERROR_RANGE = 400..499
private val SERVER_ERROR_RANGE = 500..599
