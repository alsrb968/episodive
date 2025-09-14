package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class SoundbiteResponse(
    @SerializedName("enclosureUrl") val enclosureUrl: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("startTime") val startTime: Long,
    @SerializedName("duration") val duration: Int,
    @SerializedName("episodeId") val episodeId: Long? = null,
    @SerializedName("episodeTitle") val episodeTitle: String? = null,
    @SerializedName("feedTitle") val feedTitle: String? = null,
    @SerializedName("feedUrl") val feedUrl: String? = null,
    @SerializedName("feedId") val feedId: Long? = null,
)
