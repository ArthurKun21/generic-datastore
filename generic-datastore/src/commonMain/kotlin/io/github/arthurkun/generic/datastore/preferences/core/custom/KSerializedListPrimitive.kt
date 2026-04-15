package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

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
) : CustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { json.encodeToString(listSerializer, it) },
    deserializer = { json.decodeFromString(listSerializer, it) },
    ioDispatcher = ioDispatcher,
)
