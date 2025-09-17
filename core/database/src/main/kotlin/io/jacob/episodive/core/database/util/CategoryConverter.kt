package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.Category

class CategoryConverter {
    @TypeConverter
    fun fromCategories(categories: List<Category>): String =
        categories.joinToString(",") { it.id.toString() }

    @TypeConverter
    fun toCategories(idsString: String): List<Category> =
        if (idsString.isEmpty()) {
            emptyList()
        } else {
            idsString.split(",").mapNotNull { id ->
                Category.entries.find { it.id == id.toIntOrNull() }
            }
        }
}