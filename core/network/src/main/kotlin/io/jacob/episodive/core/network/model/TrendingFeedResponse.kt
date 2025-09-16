package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class TrendingFeedResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("url") val url: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("author") val author: String,
    @SerializedName("image") val image: String,
    @SerializedName("artwork") val artwork: String,
    @SerializedName(value = "newestItemPubdate", alternate = ["newestItemPublishTime"]) val newestItemPublishTime: Long,
    @SerializedName("itunesId") val itunesId: Long? = null,
    @SerializedName("trendScore") val trendScore: Int,
    @SerializedName("language") val language: String,
    @SerializedName("categories") val categories: Map<Int, String>? = null,
)
