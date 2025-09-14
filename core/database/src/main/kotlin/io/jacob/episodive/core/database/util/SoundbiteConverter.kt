package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.jacob.episodive.core.model.Soundbite

class SoundbiteConverter {
    @TypeConverter
    fun fromSoundbiteList(value: List<Soundbite>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSoundbiteList(value: String): List<Soundbite> {
        val type = object : TypeToken<List<Soundbite>>() {}.type
        return Gson().fromJson(value, type)
    }
}