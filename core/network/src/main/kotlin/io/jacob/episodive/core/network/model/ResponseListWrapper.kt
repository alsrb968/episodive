package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class ResponseListWrapper<T>(
    @SerializedName("status") val status: String,
    @SerializedName(value = "feeds", alternate = ["items", "episodes"]) val dataList: List<T>,
    @SerializedName("liveItems") val liveEpisodes: List<EpisodeResponse>? = null,
    @SerializedName("description") val description: String,
)
