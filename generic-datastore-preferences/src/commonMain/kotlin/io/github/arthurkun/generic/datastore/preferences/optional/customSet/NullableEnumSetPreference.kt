@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.optional.customSet

import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Internal nullable enum-set preference factory used by the public `nullableEnumSet` extension.
 *
 * Enum values are stored by [Enum.name]. Missing keys produce `null`; unknown stored names are
 * skipped.
 */
@PublishedApi
internal inline fun <reified T : Enum<T>> PreferencesDatastore.internalNullableEnumSet(
    key: String,
): Preference<Set<T>?> = nullableSerializedSet(
    key = key,
    serializer = { it.name },
    deserializer = { enumValueOf(it) },
)
