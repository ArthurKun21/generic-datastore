@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.core.Prefs
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.default.enum as preferencesEnum

/**
 * Extension for backwards compatibility.
 * @see io.github.arthurkun.generic.datastore.preferences.default.enum
 */
@Deprecated(
    message = "Moved to preferences package",
    replaceWith = ReplaceWith(
        "enum(key, defaultValue)",
        "io.github.arthurkun.generic.datastore.preferences.default.enum",
    ),
    level = DeprecationLevel.WARNING,
)
public inline fun <reified T : Enum<T>> PreferencesDatastore.enum(
    key: String,
    defaultValue: T,
): Prefs<T> = preferencesEnum(key, defaultValue)
