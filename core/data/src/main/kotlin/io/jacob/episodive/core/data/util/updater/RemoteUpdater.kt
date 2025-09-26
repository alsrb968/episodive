package io.jacob.episodive.core.data.util.updater

interface RemoteUpdater<T> {
    suspend fun load(cached: List<T>)
    suspend fun load(cached: T?)
}