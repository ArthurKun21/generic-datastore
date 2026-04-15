@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.core.customSet

import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Internal enum-set preference factory used by the public `enumSet` extension.
 *
 * Enum values are stored by [Enum.name]. Unknown stored names are skipped.
 */
@PublishedApi
internal inline fun <reified T : Enum<T>> PreferencesDatastore.internalEnumSet(
    key: String,
    defaultValue: Set<T> = emptySet(),
): Preference<Set<T>> = serializedSet(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = { enumValueOf(it) },
)
