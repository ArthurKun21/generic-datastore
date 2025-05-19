package io.github.arthurkun.generic.datastore

import android.util.Log

inline fun <reified T : Enum<T>> PreferenceDatastore.enum(
    key: String,
    defaultValue: T,
) {
    serialized(
        key = key,
        defaultValue = defaultValue,
        serializer = { it.name },
        deserializer = {
            try {
                enumValueOf(it)
            } catch (e: IllegalArgumentException) {
                Log.e(
                    TAG,
                    "Enum value $it not found for key $key, returning default value $defaultValue",
                    e
                )
                defaultValue
            }
        },
    )
}