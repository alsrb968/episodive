package io.jacob.episodive.core.database.util

import androidx.room.TypeConverters
import io.jacob.episodive.core.model.Category

class CategoryConverter {
    @TypeConverters
    fun fromCategory(category: Category?): String? =
        category?.label

    @TypeConverters
    fun fromCategories(categories: List<Category>?): String? =
        categories?.joinToString(",") { it.label }

    @TypeConverters
    fun toCategory(label: String?): Category? =
        Category.entries.find { it.label == label }

    @TypeConverters
    fun toCategories(labels: String?): List<Category>? =
        labels?.split(",")?.mapNotNull { label ->
            Category.entries.find { it.label == label }
        }
}