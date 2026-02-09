package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A [NullableCustomGenericPreferenceItem] for storing nullable custom objects using
 * [kotlinx.serialization][KSerializer].
 *
 * Values are serialized to a JSON string via the provided [json] instance and
 * [serializer], then stored as a string preference. If deserialization fails
 * (e.g., due to corrupted data), `null` is returned.
 *
 * @param T The non-null type of the custom object. Must be serializable via kotlinx.serialization.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param serializer The [KSerializer] for the type [T].
 * @param json The [Json] instance to use for serialization and deserialization.
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
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
