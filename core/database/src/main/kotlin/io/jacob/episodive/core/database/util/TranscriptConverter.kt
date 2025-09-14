package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.jacob.episodive.core.model.Transcript

class TranscriptConverter {
    @TypeConverter
    fun fromTranscriptList(value: List<Transcript>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toTranscriptList(value: String): List<Transcript> {
        val type = object : TypeToken<List<Transcript>>() {}.type
        return Gson().fromJson(value, type)
    }
}