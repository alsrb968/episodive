package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class SoundbiteResponse(
    @SerializedName("enclosureUrl") val enclosureUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: Long,
    @SerializedName("duration") val duration: Int,
    @SerializedName("episodeId") val episodeId: Long,
    @SerializedName("episodeTitle") val episodeTitle: String,
    @SerializedName("feedTitle") val feedTitle: String,
    @SerializedName("feedUrl") val feedUrl: String,
    @SerializedName("feedId") val feedId: Long,
)
