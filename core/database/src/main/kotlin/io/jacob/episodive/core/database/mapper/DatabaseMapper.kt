package io.jacob.episodive.core.database.mapper

import io.jacob.episodive.core.database.model.EpisodeEntity
import io.jacob.episodive.core.database.model.FollowedPodcastDto
import io.jacob.episodive.core.database.model.LikedEpisodeDto
import io.jacob.episodive.core.database.model.PlayedEpisodeDto
import io.jacob.episodive.core.database.model.PodcastEntity
import io.jacob.episodive.core.model.Episode
import io.jacob.episodive.core.model.FollowedPodcast
import io.jacob.episodive.core.model.LikedEpisode
import io.jacob.episodive.core.model.PlayedEpisode
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

fun Podcast.toPodcastEntity(
    cacheKey: String,
    cachedAt: Instant = Clock.System.now(),
): PodcastEntity =
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
        cacheKey = cacheKey,
        cachedAt = cachedAt,
    )

fun List<Podcast>.toPodcastEntities(
    cacheKey: String,
    cachedAt: Instant = Clock.System.now()
): List<PodcastEntity> =
    map {
        it.toPodcastEntity(
            cacheKey = cacheKey,
            cachedAt = cachedAt,
        )
    }

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

fun FollowedPodcastDto.toFollowedPodcast(): FollowedPodcast =
    FollowedPodcast(
        podcast = podcast?.toPodcast()
            ?: throw IllegalStateException("FollowedPodcastDto.podcast is null"),
        followedAt = followedAt,
        isNotificationEnabled = isNotificationEnabled,
    )

fun List<FollowedPodcastDto>.toFollowedPodcasts(): List<FollowedPodcast> =
    map { it.toFollowedPodcast() }

fun LikedEpisodeDto.toLikedEpisode(): LikedEpisode =
    LikedEpisode(
        episode = episode?.toEpisode()
            ?: throw IllegalStateException("LikedEpisodeDto.episode is null"),
        likedAt = likedAt,
    )

fun List<LikedEpisodeDto>.toLikedEpisodes(): List<LikedEpisode> =
    map { it.toLikedEpisode() }

fun PlayedEpisodeDto.toPlayedEpisode(): PlayedEpisode =
    PlayedEpisode(
        episode = episode?.toEpisode()
            ?: throw IllegalStateException("PlayedEpisodeDto.episode is null"),
        playedAt = playedAt,
        position = position,
        isCompleted = isCompleted,
    )

fun List<PlayedEpisodeDto>.toPlayedEpisodes(): List<PlayedEpisode> =
    map { it.toPlayedEpisode() }