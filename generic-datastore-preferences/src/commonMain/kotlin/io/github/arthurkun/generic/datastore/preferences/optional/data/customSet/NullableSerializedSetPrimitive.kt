package io.github.arthurkun.generic.datastore.preferences.optional.data.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * [NullableSetGenericPreferenceItem] for a nullable string-set-backed custom [Set] using
 * caller-supplied element serializers.
 */
internal class NullableSerializedSetPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)

/**
 * [NullableSetGenericPreferenceItem] that stores each set element as JSON using
 * kotlinx.serialization.
 */
internal class NullableKSerializedSetPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = { json.encodeToString(serializer, it) },
    deserializer = { json.decodeFromString(serializer, it) },
    ioDispatcher = ioDispatcher,
)
