package io.jacob.episodive.core.domain.repository

interface ImageRepository {
    suspend fun extractDominantColorFromUrl(imageUrl: String?): ULong
}