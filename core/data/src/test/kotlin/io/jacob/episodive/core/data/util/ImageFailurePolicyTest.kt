package io.jacob.episodive.core.data.util

import coil.network.HttpException
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.security.cert.CertificateExpiredException
import javax.net.ssl.SSLHandshakeException
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 어떤 실패를 기억하고 어떤 실패를 잊을지 고정한다.
 *
 * 가장 중요한 테스트는 "기록하지 않는다"쪽이다. 일시적 실패를 기록하면 지하철에서 스크롤 한 번에
 * 화면 전체가 블랙리스트로 굳는다.
 */
class ImageFailurePolicyTest {

    @Test
    fun `Given offline errors, when policy applied, then not recorded`() {
        assertNull(UnknownHostException("no dns").toImageFailureTtl())
        assertNull(ConnectException("refused").toImageFailureTtl())
        assertNull(SocketTimeoutException("timeout").toImageFailureTtl())
    }

    @Test
    fun `Given not found, when policy applied, then recorded for a day`() {
        assertEquals(24.hours, httpException(404).toImageFailureTtl())
        assertEquals(24.hours, httpException(410).toImageFailureTtl())
    }

    @Test
    fun `Given hotlink block, when policy applied, then recorded for a day`() {
        assertEquals(24.hours, httpException(403).toImageFailureTtl())
        assertEquals(24.hours, httpException(401).toImageFailureTtl())
    }

    @Test
    fun `Given server error, when policy applied, then recorded briefly`() {
        assertEquals(5.minutes, httpException(500).toImageFailureTtl())
        assertEquals(5.minutes, httpException(503).toImageFailureTtl())
    }

    @Test
    fun `Given unsatisfiable request, when policy applied, then not recorded`() {
        // 504 는 서버 응답이 아니라 OkHttp 가 오프라인 캐시 미스에 만드는 합성 응답으로도 온다
        // ("Unsatisfiable Request (only-if-cached)", 실기기 확인). 이걸 기록하면 비행기 모드로
        // 스크롤 한 번에 화면 전체가 차단된다.
        assertNull(httpException(504).toImageFailureTtl())
    }

    @Test
    fun `Given rate limited, when policy applied, then recorded briefly`() {
        // 429 는 4xx 지만 곧 풀리므로 영구 취급하면 안 된다.
        assertEquals(5.minutes, httpException(429).toImageFailureTtl())
    }

    @Test
    fun `Given request timeout, when policy applied, then not recorded`() {
        // 408 은 4xx 지만 "다시 물어봐도 같은 답"의 정반대다.
        assertNull(httpException(408).toImageFailureTtl())
    }

    @Test
    fun `Given certificate failure, when policy applied, then recorded for hours`() {
        val handshake = SSLHandshakeException("bad cert").apply {
            initCause(CertificateExpiredException("expired"))
        }

        assertEquals(6.hours, handshake.toImageFailureTtl())
        assertEquals(6.hours, CertificateExpiredException("expired").toImageFailureTtl())
    }

    @Test
    fun `Given handshake interrupted by network, when policy applied, then not recorded`() {
        // SSLHandshakeException 은 인증서 문제 전용이 아니다. 불안정한 망에서 핸드셰이크 도중
        // 연결이 끊겨도 같은 예외가 온다. 그것까지 기록하면 지하철에서 스크롤 한 번에 화면의
        // 커버가 전부 몇 시간씩 차단된다.
        val transport = SSLHandshakeException("Connection reset by peer")

        assertNull(transport.toImageFailureTtl())
    }

    @Test
    fun `Given cleartext blocked, when policy applied, then recorded for a day`() {
        assertEquals(24.hours, UnknownServiceException("cleartext not permitted").toImageFailureTtl())
    }

    @Test
    fun `Given unknown error, when policy applied, then not recorded`() {
        // 모르는 실패는 기록하지 않는다 — 놓친 재요청보다 잘못된 차단이 나쁘다.
        assertNull(IllegalStateException("???").toImageFailureTtl())
        assertNull(RuntimeException().toImageFailureTtl())
    }

    private fun httpException(code: Int) = HttpException(
        Response.Builder()
            .request(Request.Builder().url("https://example.com/a.jpg").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("test")
            .build()
    )
}
