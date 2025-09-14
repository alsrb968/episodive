package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class PersonResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("group") val group: String,
    @SerializedName("href") val href: String,
    @SerializedName("image") val image: String,
)
