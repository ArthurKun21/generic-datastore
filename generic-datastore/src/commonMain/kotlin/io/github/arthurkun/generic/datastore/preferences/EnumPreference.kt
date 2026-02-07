package io.github.arthurkun.generic.datastore.preferences

import io.github.arthurkun.generic.datastore.core.Prefs

/**
 * Defines a preference for storing enum values.
 *
 * This function serializes the enum value to its name for storage and deserializes
 * the stored string back to the enum value. If deserialization fails due to an
 * unknown enum value, it logs an error and returns the `defaultValue`.
 *
 * @param T The enum type.
 * @param key The key for the preference.
 * @param defaultValue The default enum value to use if the key is not found or
 * deserialization fails.
 */
@Suppress("unused")
public inline fun <reified T : Enum<T>> PreferencesDatastore.enum(
    key: String,
    defaultValue: T,
): Prefs<T> = serialized(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = {
        try {
            enumValueOf(it)
        } catch (_: IllegalArgumentException) {
            defaultValue
        }
    },
)
