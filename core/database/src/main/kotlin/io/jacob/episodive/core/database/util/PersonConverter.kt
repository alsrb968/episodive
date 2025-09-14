package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.jacob.episodive.core.model.Person

class PersonConverter {
    @TypeConverter
    fun fromPersonList(value: List<Person>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPersonList(value: String): List<Person> {
        val type = object : TypeToken<List<Person>>() {}.type
        return Gson().fromJson(value, type)
    }
}