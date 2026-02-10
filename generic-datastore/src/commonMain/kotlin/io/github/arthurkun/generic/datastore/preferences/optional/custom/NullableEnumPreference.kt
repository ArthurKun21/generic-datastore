@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.optional.custom

import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.preferences.Preferences
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Creates a nullable [DelegatedPreference] for storing enum values.
 *
 * The enum is serialized using its [name][Enum.name] and deserialized via
 * [enumValueOf]. If the stored string does not match any enum constant,
 * `null` is returned.
 *
 * @param T The enum type.
 * @param key The unique string key for the preference.
 * @return A [DelegatedPreference] instance backed by [PreferencesDatastore.nullableSerialized].
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.nullableEnum(
    key: String,
): Preferences<T?> = nullableSerialized(
    key = key,
    serializer = { it.name },
    deserializer = {
        enumValueOf(it)
    },
)
