package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class RecentNewFeedResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("timeAdded") val timeAdded: Long,
    @SerializedName("status") val status: String,
    @SerializedName("contentHash") val contentHash: String,
    @SerializedName("language") val language: String,
)
