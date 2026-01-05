package io.github.arthurkun.generic.datastore

import logcat.logcat

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
inline fun <reified T : Enum<T>> PreferenceDatastore.enum(
    key: String,
    defaultValue: T,
): Prefs<T> = serialized(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = {
        try {
            enumValueOf(it)
        } catch (e: IllegalArgumentException) {
            logcat(TAG) {
                "Enum value $it not found for key $key, " +
                    "returning default value $defaultValue ${e.message}"
            }
            defaultValue
        }
    },
)
