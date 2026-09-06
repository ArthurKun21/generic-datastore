package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeList
import io.github.arthurkun.generic.datastore.preferences.utils.serializeList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [CustomGenericPreferenceItem] that stores a [List] inside one JSON array string.
 *
 * The outer JSON parse falls back to [defaultValue]. Individual elements that fail to deserialize
 * are skipped.
 */
internal class SerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: List<T>,
    elementSerializer: (T) -> String,
    elementDeserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { list -> serializeList(list, elementSerializer) },
    deserializer = { str -> deserializeList(str, elementDeserializer) },
    ioDispatcher = ioDispatcher,
)
