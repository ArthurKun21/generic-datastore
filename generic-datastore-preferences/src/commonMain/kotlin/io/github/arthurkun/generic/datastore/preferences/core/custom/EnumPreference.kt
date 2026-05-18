@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.core.custom

import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Internal enum preference factory used by the public `enum` extension.
 *
 * Enum values are stored by [Enum.name]. Unknown stored names fall back to [defaultValue].
 */
@PublishedApi
internal inline fun <reified T : Enum<T>> PreferencesDatastore.internalEnum(
    key: String,
    defaultValue: T,
): Preference<T> = serialized(
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = {
        enumValueOf(it)
    },
)
