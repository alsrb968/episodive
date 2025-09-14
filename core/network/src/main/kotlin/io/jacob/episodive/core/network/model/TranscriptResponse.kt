package io.jacob.episodive.core.network.model

import com.google.gson.annotations.SerializedName

data class TranscriptResponse(
    @SerializedName("url") val url: String,
    @SerializedName("type") val type: String,
)

/*
"transcripts": [
    {
        "url": "https://mp3s.nashownotes.com/NA-1796-Captions.srt",
        "type": "application/srt"
    }
]
 */