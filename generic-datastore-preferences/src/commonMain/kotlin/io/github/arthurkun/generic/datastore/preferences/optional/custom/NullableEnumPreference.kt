@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.optional.custom

import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import io.github.arthurkun.generic.datastore.preferences.Preference
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * Internal nullable enum preference factory used by the public `nullableEnum` extension.
 *
 * Enum values are stored by [Enum.name]. Missing keys and unknown stored names return `null`.
 */
@PublishedApi
internal inline fun <reified T : Enum<T>> PreferencesDatastore.internalNullableEnum(
    key: String,
): Preference<T?> = nullableSerialized(
    key = key,
    serializer = { it.name },
    deserializer = {
        enumValueOf(it)
    },
)
