package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
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
) : NullableCustomGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = { json.encodeToString(serializer, it) },
    deserializer = { json.decodeFromString(serializer, it) },
    ioDispatcher = ioDispatcher,
)
