package io.jacob.episodive.core.model.mapper

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.EpisodeType
import io.jacob.episodive.core.model.Feed
import io.jacob.episodive.core.model.Medium
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.TrendingFeed
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun Long.toInstant(): Instant = Instant.fromEpochSeconds(this)
fun Instant.toSeconds(): Long = epochSeconds

fun Int.toDurationSeconds(): Duration = seconds
fun Duration.toIntSeconds(): Int = inWholeSeconds.toInt()

fun String.toMedium(): Medium? = Medium.entries.find { it.value == this }
fun Medium.toValue(): String = value

fun Map<Int, String>.toCategories(): List<Category> =
    this.mapNotNull { (id, _) ->
        Category.entries.find { it.id == id }
    }.toList()

fun List<Category>.toMap(): Map<Int, String> =
    associateBy({ it.id }, { it.label })

fun List<Category>.toCommaString(): String =
    sortedBy { it.id }.joinToString(",") { it.id.toString() }

fun String.toCategories(): List<Category> =
    if (this.isEmpty()) {
        emptyList()
    } else {
        this.split(",").mapNotNull { id ->
            Category.entries.find { it.id == id.toIntOrNull() }
        }
    }

fun String.toEpisodeType(): EpisodeType? = EpisodeType.entries.find { it.value == this }
fun EpisodeType.toValue(): String = value

fun RecentFeed.toFeedFromRecent(): Feed =
    Feed(
        id = id,
        url = url,
        title = title,
        newestItemPublishTime = newestItemPublishTime,
        description = description,
        author = author,
        image = image,
        itunesId = itunesId,
        trendScore = trendScore,
        language = language,
        categories = categories,
    )

fun List<RecentFeed>.toFeedsFromRecent(): List<Feed> = map { it.toFeedFromRecent() }

fun TrendingFeed.toFeedFromTrending(): Feed =
    Feed(
        id = id,
        url = url,
        title = title,
        newestItemPublishTime = newestItemPublishTime,
        description = description,
        author = author,
        image = artwork,
        itunesId = itunesId,
        trendScore = trendScore,
        language = language,
        categories = categories,
    )

fun List<TrendingFeed>.toFeedsFromTrending(): List<Feed> = map { it.toFeedFromTrending() }