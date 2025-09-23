package io.jacob.episodive.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val language = stringPreferencesKey("language")
    val categories = stringPreferencesKey("categories")
}