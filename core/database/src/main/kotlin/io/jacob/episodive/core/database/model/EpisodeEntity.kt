package io.jacob.episodive.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.EpisodeType
import io.jacob.episodive.core.model.Person
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.Transcript
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: Long,
    val guid: String,
    val title: String,
    val link: String,
    val description: String,
    val datePublished: Instant,
    val dateCrawled: Instant,
    val enclosureUrl: String,
    val enclosureType: String,
    val enclosureLength: Int,
    val startTime: Instant,
    val endTime: Instant,
    val status: String,
    val contentLink: String? = null,
    val duration: Duration? = null,
    val explicit: Int,
    val episode: Int? = null,
    val episodeType: EpisodeType? = null,
    val season: Int? = null,
    val image: String,
    val podcastGuid: String? = null,
    val feedItunesId: Long? = null,
    val feedImage: String,
    val feedId: Long,
    val feedUrl: String? = null,
    val feedAuthor: String? = null,
    val feedTitle: String? = null,
    val feedLanguage: String,
    val feedDuplicateOf: Long? = null,
    val chaptersUrl: String? = null,
    val transcriptUrl: String? = null,
    val transcripts: List<Transcript>? = null,
    val soundbites: List<Soundbite>? = null,
    val persons: List<Person>? = null,
    val categories: List<Category>? = null,
)
