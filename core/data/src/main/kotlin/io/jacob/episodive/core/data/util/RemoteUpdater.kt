package io.jacob.episodive.core.data.util

interface RemoteUpdater<T> {
    suspend fun load(cached: List<T>)
}