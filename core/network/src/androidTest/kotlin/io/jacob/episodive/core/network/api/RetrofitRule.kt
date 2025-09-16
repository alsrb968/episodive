package io.jacob.episodive.core.network.api

import io.jacob.episodive.core.network.util.EpisodiveInterceptor
import io.jacob.episodive.core.network.util.RETROFIT_BASE_URL
import okhttp3.OkHttpClient
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitRule : TestWatcher() {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(EpisodiveInterceptor())
        .build()

    lateinit var retrofit: Retrofit
        private set

    override fun starting(description: Description?) {
        retrofit = Retrofit.Builder()
            .baseUrl(RETROFIT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}