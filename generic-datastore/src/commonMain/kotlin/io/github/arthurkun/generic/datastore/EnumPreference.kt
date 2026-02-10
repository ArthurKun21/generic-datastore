@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.PreferencesPrefs
import io.github.arthurkun.generic.datastore.preferences.default.custom.enum as preferencesEnum

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.default.custom.enum
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
): PreferencesPrefs<T> = preferencesEnum(key, defaultValue)
