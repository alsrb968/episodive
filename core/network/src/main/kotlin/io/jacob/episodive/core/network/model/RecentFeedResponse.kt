package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class RecentFeedResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("title") val title: String,
    @SerializedName(value = "newestItemPubdate", alternate = ["newestItemPublishTime"]) val newestItemPublishTime: Long,
    @SerializedName("oldestItemPublishTime") val oldestItemPublishTime: Long? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("itunesId") val itunesId: Long? = null,
    @SerializedName("trendScore") val trendScore: Int? = null,
    @SerializedName("language") val language: String,
    @SerializedName("categories") val categories: Map<Int, String>? = null,
)