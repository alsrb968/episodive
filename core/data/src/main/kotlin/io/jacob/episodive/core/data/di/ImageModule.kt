package io.jacob.episodive.core.data.di

import android.content.Context
import android.content.pm.ApplicationInfo
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.data.util.ImageCacheInterceptor
import io.jacob.episodive.core.data.util.ImageFailureCache
import io.jacob.episodive.core.data.util.ImageRequestInterceptor
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageLoaderOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {
    @Provides
    @Singleton
    @ImageLoaderOkHttpClient
    fun provideImageLoaderOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ImageCacheInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        @ImageLoaderOkHttpClient okHttpClient: OkHttpClient,
        imageFailureCache: ImageFailureCache,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .components { add(ImageRequestInterceptor(imageFailureCache)) }
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512 * 1024 * 1024) // 512MB
                    .build()
            }
            .okHttpClient(okHttpClient)
            .apply {
                // DebugLogger 는 요청마다 로그를 쓴다. 릴리스 빌드에서는 순수한 낭비이고,
                // 스크롤 한 번에 수십 줄이 쌓여 정작 봐야 할 로그를 덮는다.
                if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
