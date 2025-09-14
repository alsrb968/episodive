package io.jacob.episodive.core.network

import io.jacob.episodive.core.network.util.PodcastIndexInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PodcastIndexClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor(PodcastIndexInterceptor())
        .build()

    private const val BASE_URL = "https://api.podcastindex.org/api/1.0/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}