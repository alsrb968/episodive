package io.jacob.episodive.core.data.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class Cacher<T> (
    private val remoteUpdater: RemoteUpdater<T>,
    private val sourceFactory: () -> Flow<List<T>>,
) {

    /**
     * 로컬 데이터를 먼저 내보내고, 만료시 백그라운드에서 갱신
     */
    val flow: Flow<List<T>> = flow {
        val source = sourceFactory()

        // 백그라운드에서 만료 체크 및 갱신
        coroutineScope {
            launch {
                val cachedData = source.first()
                remoteUpdater.load(cachedData)
            }
        }

        // 로컬 데이터 스트림을 그대로 전달
        source.collect { data ->
            emit(data)
        }
    }
}