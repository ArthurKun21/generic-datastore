@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.default.custom

import io.github.arthurkun.generic.datastore.core.Prefs
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Creates a [Prefs] for storing enum values.
 *
 * The enum is serialized using its [name][Enum.name] and deserialized via
 * [enumValueOf]. If the stored string does not match any enum constant,
 * the [defaultValue] is returned.
 *
 * @param T The enum type.
 * @param key The unique string key for the preference.
 * @param defaultValue The default enum value to use if the key is not found or
 *   deserialization fails.
 * @return A [Prefs] instance backed by [PreferencesDatastore.serialized].
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.enum(
    key: String,
    defaultValue: T,
): Prefs<T> = serialized(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = {
        enumValueOf(it)
    },
)
