@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.core.customSet

import io.github.arthurkun.generic.datastore.preferences.Preferences
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Defines a preference for storing a [Set] of enum values using a string set preference key.
 *
 * Each enum value is serialized to its name for storage and deserialized back on retrieval.
 * If deserialization of an individual element fails due to an unknown enum value, that element
 * is skipped.
 *
 * @param T The enum type.
 * @param key The key for the preference.
 * @param defaultValue The default set of enum values to use if the key is not found
 * (defaults to an empty set).
 */
public inline fun <reified T : Enum<T>> PreferencesDatastore.enumSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
): Preferences<Set<T>> = serializedSet(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = { enumValueOf(it) },
)
