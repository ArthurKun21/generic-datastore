package io.github.arthurkun.generic.datastore.preferences.core.custom

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
 * [CustomGenericPreferenceItem] that stores a single custom value as JSON using
 * kotlinx.serialization.
 */
internal class KSerializedPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T, String>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key),
    serializer = { json.encodeToString(serializer, it) },
    deserializer = { json.decodeFromString(serializer, it) },
    ioDispatcher = ioDispatcher,
)

/**
 * [CustomGenericPreferenceItem] that stores a [List] as one JSON array string using
 * kotlinx.serialization.
 */
internal class KSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: List<T>,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    listSerializer: KSerializer<List<T>> = ListSerializer(serializer),
) : CustomGenericPreferenceItem<List<T>, String>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key),
    serializer = { json.encodeToString(listSerializer, it) },
    deserializer = { json.decodeFromString(listSerializer, it) },
    ioDispatcher = ioDispatcher,
)
