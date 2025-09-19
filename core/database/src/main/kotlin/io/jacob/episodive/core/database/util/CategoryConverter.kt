package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.mapper.toCategories
import io.jacob.episodive.core.model.mapper.toCommaString

class CategoryConverter {
    @TypeConverter
    fun fromCategories(categories: List<Category>): String =
        categories.toCommaString()

    @TypeConverter
    fun toCategories(idsString: String): List<Category> =
        idsString.toCategories()
}