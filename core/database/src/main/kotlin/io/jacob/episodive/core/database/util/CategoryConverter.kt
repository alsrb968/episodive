package io.jacob.episodive.core.database.util

import androidx.room.TypeConverter
import io.jacob.episodive.core.model.Category

class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: Category?): String? =
        category?.label

    @TypeConverter
    fun fromCategories(categories: List<Category>?): String? =
        categories?.joinToString(",") { it.label }

    @TypeConverter
    fun toCategory(label: String?): Category? =
        Category.entries.find { it.label == label }

    @TypeConverter
    fun toCategories(labels: String?): List<Category>? =
        labels?.split(",")?.mapNotNull { label ->
            Category.entries.find { it.label == label }
        }
}