package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class RecentNewValueFeedResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("image") val image: String,
    @SerializedName(value = "newestItemPubdate", alternate = ["newestItemPublishTime"]) val newestItemPublishTime: Long,
    @SerializedName("itunesId") val itunesId: Long? = null,
    @SerializedName("trendScore") val trendScore: Int,
    @SerializedName("language") val language: String,
    @SerializedName("categories") val categories: Map<Int, String>? = null,
)
