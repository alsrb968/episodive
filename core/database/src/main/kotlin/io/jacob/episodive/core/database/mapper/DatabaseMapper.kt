package io.jacob.episodive.core.database.mapper

import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.Podcast
import kotlin.time.Clock
import kotlin.time.Instant

fun PodcastEntity.toPodcast(): Podcast =
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
        lastUpdateTime = lastUpdateTime,
        lastCrawlTime = lastCrawlTime,
        lastParseTime = lastParseTime,
        lastGoodHttpStatusTime = lastGoodHttpStatusTime,
        lastHttpStatus = lastHttpStatus,
        contentType = contentType,
        itunesId = itunesId,
        itunesType = itunesType,
        generator = generator,
        language = language,
        explicit = explicit,
        type = type,
        medium = medium,
        dead = dead,
        chash = chash,
        episodeCount = episodeCount,
        crawlErrors = crawlErrors,
        parseErrors = parseErrors,
        categories = categories,
        locked = locked,
        imageUrlHash = imageUrlHash,
        newestItemPublishTime = newestItemPublishTime,
    )

fun List<PodcastEntity>.toPodcasts(): List<Podcast> =
    map { it.toPodcast() }

fun Podcast.toPodcastEntity(): PodcastEntity =
    PodcastEntity(
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
        lastUpdateTime = lastUpdateTime,
        lastCrawlTime = lastCrawlTime,
        lastParseTime = lastParseTime,
        lastGoodHttpStatusTime = lastGoodHttpStatusTime,
        lastHttpStatus = lastHttpStatus,
        contentType = contentType,
        itunesId = itunesId,
        itunesType = itunesType,
        generator = generator,
        language = language,
        explicit = explicit,
        type = type,
        medium = medium,
        dead = dead,
        chash = chash,
        episodeCount = episodeCount,
        crawlErrors = crawlErrors,
        parseErrors = parseErrors,
        categories = categories,
        locked = locked,
        imageUrlHash = imageUrlHash,
        newestItemPublishTime = newestItemPublishTime,
    )

fun List<Podcast>.toPodcastEntities(): List<PodcastEntity> =
    map { it.toPodcastEntity() }

fun EpisodeEntity.toEpisode(): Episode =
    Episode(
        id = id,
        guid = guid,
        title = title,
        link = link,
        description = description,
        datePublished = datePublished,
        dateCrawled = dateCrawled,
        enclosureUrl = enclosureUrl,
        enclosureType = enclosureType,
        enclosureLength = enclosureLength,
        startTime = startTime,
        endTime = endTime,
        status = status,
        contentLink = contentLink,
        duration = duration,
        explicit = explicit,
        episode = episode,
        episodeType = episodeType,
        season = season,
        image = image,
        feedItunesId = feedItunesId,
        feedImage = feedImage,
        feedId = feedId,
        feedUrl = feedUrl,
        feedAuthor = feedAuthor,
        feedTitle = feedTitle,
        feedLanguage = feedLanguage,
        categories = categories,
        chaptersUrl = chaptersUrl,
        transcriptUrl = transcriptUrl,
        transcripts = transcripts,
    )

fun List<EpisodeEntity>.toEpisodes(): List<Episode> =
    map { it.toEpisode() }

fun Episode.toEpisodeEntity(
    cacheKey: String,
    cachedAt: Instant = Clock.System.now(),
): EpisodeEntity =
    EpisodeEntity(
        id = id,
        guid = guid,
        title = title,
        link = link,
        description = description,
        datePublished = datePublished,
        dateCrawled = dateCrawled,
        enclosureUrl = enclosureUrl,
        enclosureType = enclosureType,
        enclosureLength = enclosureLength,
        startTime = startTime,
        endTime = endTime,
        status = status,
        contentLink = contentLink,
        duration = duration,
        explicit = explicit,
        episode = episode,
        episodeType = episodeType,
        season = season,
        image = image,
        feedItunesId = feedItunesId,
        feedImage = feedImage,
        feedId = feedId,
        feedUrl = feedUrl,
        feedAuthor = feedAuthor,
        feedTitle = feedTitle,
        feedLanguage = feedLanguage,
        categories = categories,
        chaptersUrl = chaptersUrl,
        transcriptUrl = transcriptUrl,
        transcripts = transcripts,
        cacheKey = cacheKey,
        cachedAt = cachedAt,
    )

fun List<Episode>.toEpisodeEntities(
    cacheKey: String,
    cachedAt: Instant = Clock.System.now(),
): List<EpisodeEntity> =
    map {
        it.toEpisodeEntity(
            cacheKey = cacheKey,
            cachedAt = cachedAt,
        )
    }

fun Long.toInstant(): Instant = Instant.fromEpochSeconds(this)
fun Instant.toLong(): Long = this.epochSeconds