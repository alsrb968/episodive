package io.jacob.episodive.core.model

import kotlin.time.Duration
import kotlin.time.Instant

data class Episode(
    val id: Long,
    val title: String,
    val link: String,
    val description: String? = null,
    val guid: String,
    val datePublished: Instant,
    val dateCrawled: Instant,
    val enclosureUrl: String,
    val enclosureType: String,
    val enclosureLength: Long, // in bytes, 0 is live
    val startTime: Instant? = null, // for live episodes
    val endTime: Instant? = null, // for live episodes
    val status: String? = null, // for live episodes
    val contentLink: String? = null, // for live episodes
    val duration: Duration? = null,
    val explicit: Boolean,
    val episode: Int? = null,
    val episodeType: EpisodeType? = null,
    val season: Int? = null,
    val image: String,
    val feedItunesId: Long? = null,
    val feedImage: String,
    val feedId: Long,
    val feedUrl: String? = null,
    val feedAuthor: String? = null,
    val feedTitle: String? = null,
    val feedLanguage: String,
    val categories: List<Category> = emptyList(),
    val chaptersUrl: String? = null,
    val transcriptUrl: String? = null,
    val transcripts: List<Transcript>? = null,
)
