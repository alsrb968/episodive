package io.jacob.episodive.core.network.mapper

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.EpisodeType
import io.jacob.episodive.core.model.Medium
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.Transcript
import io.jacob.episodive.core.model.TrendingFeed
import io.jacob.episodive.core.network.model.EpisodeResponse
import io.jacob.episodive.core.network.model.PodcastResponse
import io.jacob.episodive.core.network.model.RecentFeedResponse
import io.jacob.episodive.core.network.model.RecentNewFeedResponse
import io.jacob.episodive.core.network.model.RecentNewValueFeedResponse
import io.jacob.episodive.core.network.model.SoundbiteResponse
import io.jacob.episodive.core.network.model.TranscriptResponse
import io.jacob.episodive.core.network.model.TrendingFeedResponse
import kotlin.collections.joinToString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun PodcastResponse.toPodcast(): Podcast =
    Podcast(
        id = id,
        podcastGuid = podcastGuid,
        title = title,
        url = url,
        originalUrl = originalUrl,
        link = link,
        description = description,
        author = author,
        ownerName = ownerName,
        image = image,
        artwork = artwork,
        lastUpdateTime = lastUpdateTime.toInstant(),
        lastCrawlTime = lastCrawlTime.toInstant(),
        lastParseTime = lastParseTime.toInstant(),
        lastGoodHttpStatusTime = lastGoodHttpStatusTime.toInstant(),
        lastHttpStatus = lastHttpStatus,
        contentType = contentType,
        itunesId = itunesId,
        itunesType = itunesType,
        generator = generator,
        language = language,
        explicit = explicit,
        type = type,
        medium = medium?.toMedium(),
        dead = dead,
        chash = chash,
        episodeCount = episodeCount,
        crawlErrors = crawlErrors,
        parseErrors = parseErrors,
        categories = categories?.toCategories(),
        locked = locked,
        imageUrlHash = imageUrlHash,
        newestItemPublishTime = newestItemPublishTime?.toInstant(),
    )

fun List<PodcastResponse>.toPodcasts(): List<Podcast> =
    map { it.toPodcast() }

fun EpisodeResponse.toEpisode(): Episode =
    Episode(
        id = id,
        guid = guid,
        title = title,
        link = link,
        description = description,
        datePublished = datePublished.toInstant(),
        dateCrawled = dateCrawled.toInstant(),
        enclosureUrl = enclosureUrl,
        enclosureType = enclosureType,
        enclosureLength = enclosureLength,
        startTime = startTime?.toInstant(),
        endTime = endTime?.toInstant(),
        status = status,
        contentLink = contentLink,
        duration = duration?.toDuration(),
        explicit = explicit == 1,
        episode = episode,
        episodeType = episodeType?.toEpisodeType(),
        season = season,
        image = image,
        feedItunesId = feedItunesId,
        feedImage = feedImage,
        feedId = feedId,
        feedUrl = feedUrl,
        feedAuthor = feedAuthor,
        feedTitle = feedTitle,
        feedLanguage = feedLanguage,
        chaptersUrl = chaptersUrl,
        transcriptUrl = transcriptUrl,
        transcripts = transcripts?.toTranscripts(),
    )

fun List<EpisodeResponse>.toEpisodes(): List<Episode> =
    map { it.toEpisode() }

fun TrendingFeedResponse.toTrendingFeed(): TrendingFeed =
    TrendingFeed(
        id = id,
        url = url,
        title = title,
        description = description,
        author = author,
        image = image,
        artwork = artwork,
        newestItemPublishTime = newestItemPublishTime.toInstant(),
        itunesId = itunesId,
        trendScore = trendScore,
        language = language,
        categories = categories?.toCategories(),
    )

fun List<TrendingFeedResponse>.toTrendingFeeds(): List<TrendingFeed> =
    map { it.toTrendingFeed() }

fun RecentFeedResponse.toRecentFeed(): RecentFeed =
    RecentFeed(
        id = id,
        url = url,
        title = title,
        newestItemPublishTime = newestItemPublishTime.toInstant(),
        oldestItemPublishTime = oldestItemPublishTime?.toInstant(),
        description = description,
        author = author,
        itunesId = itunesId,
        trendScore = trendScore,
        language = language,
        categories = categories?.toCategories(),
    )

fun List<RecentFeedResponse>.toRecentFeeds(): List<RecentFeed> =
    map { it.toRecentFeed() }

fun RecentNewFeedResponse.toRecentNewFeed(): RecentNewFeed =
    RecentNewFeed(
        id = id,
        url = url,
        timeAdded = timeAdded.toInstant(),
        status = status,
        contentHash = contentHash,
        language = language,
    )

fun List<RecentNewFeedResponse>.toRecentNewFeeds(): List<RecentNewFeed> =
    map { it.toRecentNewFeed() }

fun RecentNewValueFeedResponse.toRecentNewValueFeed(): RecentNewValueFeed =
    RecentNewValueFeed(
        id = id,
        url = url,
        title = title,
        author = author,
        image = image,
        newestItemPublishTime = newestItemPublishTime.toInstant(),
        itunesId = itunesId,
        trendScore = trendScore,
        language = language,
        categories = categories?.toCategories(),
    )

fun List<RecentNewValueFeedResponse>.toRecentNewValueFeeds(): List<RecentNewValueFeed> =
    map { it.toRecentNewValueFeed() }

fun SoundbiteResponse.toSoundbite(): Soundbite =
    Soundbite(
        enclosureUrl = enclosureUrl,
        title = title,
        startTime = startTime.toInstant(),
        duration = duration.toDuration(),
        episodeId = episodeId,
        episodeTitle = episodeTitle,
        feedTitle = feedTitle,
        feedUrl = feedUrl,
        feedId = feedId,
    )

fun List<SoundbiteResponse>.toSoundbites(): List<Soundbite> =
    map { it.toSoundbite() }

fun Long.toInstant(): Instant =
    Instant.fromEpochSeconds(this)

fun Instant.toLong(): Long = epochSeconds

fun String.toMedium(): Medium? = Medium.entries.find { it.value == this }

fun Map<Int, String>.toCategories(): List<Category> =
    this.mapNotNull { (id, _) ->
        Category.entries.find { it.id == id }
    }.toList()

fun List<Category>.toCommaString(): String =
    this.joinToString(",") { it.label }

fun Int.toDuration(): Duration = this.seconds

fun String.toEpisodeType(): EpisodeType = EpisodeType.valueOf(this)

fun TranscriptResponse.toTranscript(): Transcript =
    Transcript(
        url = url,
        type = type,
    )

fun List<TranscriptResponse>.toTranscripts(): List<Transcript> =
    map { it.toTranscript() }