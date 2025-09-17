package io.jacob.episodive.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class EpisodeWithResumeDto(
    @Embedded
    val episode: EpisodeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
    )
    val resume: ResumeEpisodeEntity?
)