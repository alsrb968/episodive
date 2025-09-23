package io.jacob.episodive.core.datastore.mapper

import io.jacob.episodive.core.datastore.model.UserPreferences
import io.jacob.episodive.core.model.UserData

fun UserPreferences.toUserData(): UserData =
    UserData(
        language = language,
        categories = categories,
    )

fun UserData.toUserPreferences(): UserPreferences =
    UserPreferences(
        language = language,
        categories = categories,
    )