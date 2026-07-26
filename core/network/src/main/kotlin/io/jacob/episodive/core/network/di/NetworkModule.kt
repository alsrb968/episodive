package io.jacob.episodive.core.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.network.BuildConfig
import io.jacob.episodive.core.network.model.ResponseWrapper
import io.jacob.episodive.core.network.model.ResponseWrapperDeserializer
import io.jacob.episodive.core.network.util.EpisodiveInterceptor
import io.jacob.episodive.core.network.util.RETROFIT_BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitOkHttpClient

private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 30L
private const val WRITE_TIMEOUT_SECONDS = 15L

/** 단계별 타임아웃과 별개로 요청 하나가 쓸 수 있는 전체 상한. */
private const val CALL_TIMEOUT_SECONDS = 40L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @RetrofitOkHttpClient
    fun provideRetrofitOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(EpisodiveInterceptor())
            // 기본값(각 단계 10초, 전체 무제한)에 기대지 않고 명시한다. 특히 callTimeout 은
            // 기본이 무제한이라, 단계마다 조금씩 진행되는 느린 연결에서는 어떤 타임아웃에도
            // 걸리지 않고 무한히 기다릴 수 있다. 목록 API 는 한 번에 최대 1000건을 받으므로
            // read 는 기본보다 넉넉히 준다.
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(ResponseWrapper::class.java, ResponseWrapperDeserializer())
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @RetrofitOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(RETROFIT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}