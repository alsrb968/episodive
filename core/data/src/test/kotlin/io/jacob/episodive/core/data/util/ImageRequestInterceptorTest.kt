package io.jacob.episodive.core.data.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.network.HttpException
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.size.Size
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.UnknownHostException

@RunWith(RobolectricTestRunner::class)
class ImageRequestInterceptorTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var now = 0L
    private val cache = ImageFailureCache { now }
    private val interceptor = ImageRequestInterceptor(cache)

    @Test
    fun `Given blank url, when intercepted, then never reaches engine`() = runTest {
        // Given
        val chain = chain(request(""), success())

        // When
        val result = interceptor.intercept(chain)

        // Then
        coVerify(exactly = 0) { chain.proceed(any()) }
        assertTrue(result is ErrorResult)
    }

    @Test
    fun `Given doubled slash, when intercepted, then proceeds with normalized url`() = runTest {
        // Given
        val chain = chain(request("https://example.com//images/a.jpg"), success())
        val forwarded = slot<ImageRequest>()

        // When
        interceptor.intercept(chain)

        // Then
        coVerify { chain.proceed(capture(forwarded)) }
        assertEquals("https://example.com/images/a.jpg", forwarded.captured.data)
    }

    @Test
    fun `Given permanent failure recorded, when requested again, then network is disabled`() = runTest {
        // Given — 404 는 다시 물어봐도 같은 답이 온다
        val first = chain(request(URL), error(httpException(404)))
        interceptor.intercept(first)

        // When
        val second = chain(request(URL), success())
        val forwarded = slot<ImageRequest>()
        interceptor.intercept(second)

        // Then — 요청을 끊지 않고 네트워크만 잠근다. 캐시에 그림이 있으면 그건 써야 한다.
        coVerify { second.proceed(capture(forwarded)) }
        assertEquals(CachePolicy.DISABLED, forwarded.captured.networkCachePolicy)
    }

    @Test
    fun `Given blocked request served from cache, when succeeded, then record is kept`() = runTest {
        // Given
        interceptor.intercept(chain(request(URL), error(httpException(404))))

        // When — 차단 중 캐시가 받아준다
        interceptor.intercept(chain(request(URL), success()))

        // Then — 캐시 히트일 뿐 서버가 고쳐진 게 아니므로 잠금이 유지돼야 한다
        val third = chain(request(URL), success())
        val forwarded = slot<ImageRequest>()
        interceptor.intercept(third)

        coVerify { third.proceed(capture(forwarded)) }
        assertEquals(CachePolicy.DISABLED, forwarded.captured.networkCachePolicy)
    }

    @Test
    fun `Given offline failure, when requested again, then still reaches engine`() = runTest {
        // Given — 이 테스트가 깨지면 지하철에서 화면 전체가 블랙리스트로 굳는다
        val first = chain(request(URL), error(UnknownHostException("no dns")))
        interceptor.intercept(first)

        // When
        val second = chain(request(URL), success())
        val result = interceptor.intercept(second)

        // Then
        coVerify(exactly = 1) { second.proceed(any()) }
        assertTrue(result is SuccessResult)
    }

    @Test
    fun `Given ttl expired, when requested again, then network is enabled again`() = runTest {
        // Given
        interceptor.intercept(chain(request(URL), error(httpException(500))))

        // When — 5분 뒤
        now = 5 * 60 * 1000L
        val second = chain(request(URL), success())
        val forwarded = slot<ImageRequest>()
        interceptor.intercept(second)

        // Then
        coVerify { second.proceed(capture(forwarded)) }
        assertEquals(CachePolicy.ENABLED, forwarded.captured.networkCachePolicy)
    }

    @Test
    fun `Given recorded failure, when it later succeeds over network, then record is dropped`() = runTest {
        // Given
        interceptor.intercept(chain(request(URL), error(httpException(500))))
        now = 5 * 60 * 1000L

        // When — TTL 이 지나 네트워크로 성공했다
        interceptor.intercept(chain(request(URL), success()))

        // Then — 기록이 지워져 다음 요청도 네트워크를 쓸 수 있어야 한다
        now = 6 * 60 * 1000L
        val third = chain(request(URL), success())
        val forwarded = slot<ImageRequest>()
        interceptor.intercept(third)

        coVerify { third.proceed(capture(forwarded)) }
        assertEquals(CachePolicy.ENABLED, forwarded.captured.networkCachePolicy)
    }

    @Test
    fun `Given non string data, when intercepted, then passes through untouched`() = runTest {
        // Given — 리소스 ID 처럼 URL 이 아닌 데이터는 이 인터셉터가 다룰 것이 없다
        val chain = chain(request(RESOURCE_ID), success())

        // When
        val result = interceptor.intercept(chain)

        // Then
        coVerify(exactly = 1) { chain.proceed(any()) }
        assertTrue(result is SuccessResult)
    }

    private fun request(data: Any) = ImageRequest.Builder(context).data(data).build()

    private fun chain(request: ImageRequest, result: ImageResult): Interceptor.Chain = mockk {
        every { this@mockk.request } returns request
        every { size } returns Size.ORIGINAL
        coEvery { proceed(any()) } returns result
    }

    private fun success() = SuccessResult(
        drawable = ColorDrawable(),
        request = request(URL),
        dataSource = DataSource.NETWORK,
    )

    private fun error(throwable: Throwable) = ErrorResult(
        drawable = null,
        request = request(URL),
        throwable = throwable,
    )

    private fun httpException(code: Int) = HttpException(
        Response.Builder()
            .request(Request.Builder().url(URL).build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("test")
            .build()
    )

    private companion object {
        const val URL = "https://example.com/a.jpg"
        const val RESOURCE_ID = 12345
    }
}
