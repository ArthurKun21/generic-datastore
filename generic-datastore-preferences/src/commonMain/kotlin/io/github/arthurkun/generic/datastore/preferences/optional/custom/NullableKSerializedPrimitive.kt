package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * [NullableCustomGenericPreferenceItem] that stores a nullable custom value as JSON using
 * kotlinx.serialization.
 */
internal class NullableKSerializedPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T, String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
    serializer = { json.encodeToString(serializer, it) },
    deserializer = { json.decodeFromString(serializer, it) },
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableCustomGenericPreferenceItem] that stores a nullable [List] as one JSON array string
 * using kotlinx.serialization.
 */
internal class NullableKSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    listSerializer: KSerializer<List<T>> = ListSerializer(serializer),
) : NullableCustomGenericPreferenceItem<List<T>, String>(
    datastore = datastore,
    key = key,
    preferences = stringPreferencesKey(key),
    serializer = { json.encodeToString(listSerializer, it) },
    deserializer = { json.decodeFromString(listSerializer, it) },
    ioDispatcher = ioDispatcher,
)
