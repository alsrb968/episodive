package io.jacob.episodive.core.data.util.updater

import io.jacob.episodive.core.data.util.query.CacheableQuery

abstract class BaseRemoteUpdater<Entity, Query : CacheableQuery, Response>(
    protected open val query: Query,
) : RemoteUpdater<Entity> {

    abstract suspend fun fetchFromNetwork(query: Query): List<Response>
    abstract suspend fun mapToEntities(responses: List<Response>, cacheKey: String): List<Entity>
    abstract suspend fun saveToLocal(entities: List<Entity>)
    abstract suspend fun isExpired(cached: List<Entity>): Boolean

    override suspend fun load(cached: List<Entity>) {
        try {
            if (isExpired(cached)) {
                val responses = fetchFromNetwork(query)
                val entities = mapToEntities(responses, query.key)
                saveToLocal(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}