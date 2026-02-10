@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.Preferences
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.custom.enum as preferencesEnum

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.core.custom.enum
 */
@Deprecated(
    message = "Moved to preferences package",
    replaceWith = ReplaceWith(
        "enum(key, defaultValue)",
        "io.github.arthurkun.generic.datastore.preferences.default.custom.enum",
    ),
    level = DeprecationLevel.WARNING,
)
public inline fun <reified T : Enum<T>> PreferencesDatastore.enum(
    key: String,
    defaultValue: T,
): Preferences<T> = preferencesEnum(key, defaultValue)
