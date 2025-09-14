package io.jacob.episodive.core.network.util

import io.jacob.episodive.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

class PodcastIndexInterceptor(
    private val apiKey: String = BuildConfig.API_KEY,
    private val apiSecret: String = BuildConfig.SECRET_KEY,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val unixTime = System.currentTimeMillis() / 1000
        val authString = "$apiKey$apiSecret$unixTime"
        val authorization = authString.sha1()

        val request = chain.request().newBuilder()
            .addHeader("User-Agent", "Episodive/1.0")
            .addHeader("X-Auth-Date", unixTime.toString())
            .addHeader("X-Auth-Key", apiKey)
            .addHeader("Authorization", authorization)
            .build()

        return chain.proceed(request)
    }
}

private fun String.sha1(): String {
    return MessageDigest.getInstance("SHA-1")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
}