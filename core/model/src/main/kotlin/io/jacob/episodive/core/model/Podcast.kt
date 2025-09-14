package io.jacob.episodive.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Podcast(
    val id: Long,
    val podcastGuid: String,
    val title: String,
    val url: String,
    val originalUrl: String,
    val link: String,
    val description: String,
    val author: String,
    val ownerName: String,
    val image: String,
    val artwork: String,
    val lastUpdateTime: Instant,
    val lastCrawlTime: Instant,
    val lastParseTime: Instant,
    val lastGoodHttpStatusTime: Instant,
    val lastHttpStatus: Int,
    val contentType: String,
    val itunesId: Long? = null,
    val itunesType: String? = null,
    val generator: String? = null,
    val language: String,
    val explicit: Boolean? = null,
    val type: Int,
    val medium: Medium? = null,
    val dead: Int,
    val chash: String? = null,
    val episodeCount: Int,
    val crawlErrors: Int,
    val parseErrors: Int,
    val categories: List<Category>? = null,
    val locked: Int,
    val imageUrlHash: Long? = null,
//    val value: Value4Value? = null,
//    val funding: Funding? = null,
    val newestItemPubDate: Instant? = null,
)
