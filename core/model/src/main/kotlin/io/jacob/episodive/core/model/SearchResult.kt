package io.jacob.episodive.core.model

data class SearchResult(
    val podcasts: List<Podcast> = emptyList(),
    val episodes: List<Episode> = emptyList(),
)
