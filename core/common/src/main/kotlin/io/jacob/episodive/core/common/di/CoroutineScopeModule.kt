package io.jacob.episodive.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.common.ApplicationScope
import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {
    /**
     * [SupervisorJob] 이어야 한다. 여기서 도는 것들은 서로 무관한 갱신 작업이라, 하나가
     * 실패했다고 스코프가 통째로 죽으면 그 뒤로는 **앱을 다시 켤 때까지 어떤 갱신도 돌지
     * 않는다.** 실패는 각자 삼키지만 그 전에 구조적으로도 격리해 둔다.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @Dispatcher(EpisodiveDispatchers.Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
