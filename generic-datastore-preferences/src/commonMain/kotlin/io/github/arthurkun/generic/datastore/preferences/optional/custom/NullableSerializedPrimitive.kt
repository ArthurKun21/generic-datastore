package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeList
import io.github.arthurkun.generic.datastore.preferences.utils.serializeList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [NullableCustomGenericPreferenceItem] for a nullable string-backed custom value using
 * caller-supplied serializers.
 */
internal class NullableSerializedPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] that stores a nullable [List] inside one JSON array
 * string using caller-supplied element serializers.
 */
internal class NullableSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    elementSerializer: (T) -> String,
    elementDeserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<List<T>, String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
    serializer = { list -> serializeList(list, elementSerializer) },
    deserializer = { str -> deserializeList(str, elementDeserializer) },
    ioDispatcher = ioDispatcher,
)
