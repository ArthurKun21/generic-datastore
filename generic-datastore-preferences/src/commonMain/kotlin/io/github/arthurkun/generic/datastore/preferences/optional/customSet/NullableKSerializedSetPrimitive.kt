package io.github.arthurkun.generic.datastore.preferences.optional.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * [NullableCustomSetGenericPreferenceItem] that stores each nullable-set element as JSON using
 * kotlinx.serialization.
 */
internal class NullableKSerializedSetPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: KSerializer<T>,
    json: Json,
    onDecodeFailure: ((String, Throwable) -> Unit)? = null,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = { json.encodeToString(serializer, it) },
    deserializer = { json.decodeFromString(serializer, it) },
    onDecodeFailure = onDecodeFailure,
    ioDispatcher = ioDispatcher,
)
