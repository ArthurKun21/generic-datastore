package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A [NullableCustomGenericPreferenceItem] for storing a nullable [List] of custom objects
 * using [kotlinx.serialization][KSerializer].
 *
 * The list is serialized to a JSON array string via [ListSerializer] and the provided
 * [json] instance, then stored as a string preference. If deserialization fails
 * (e.g., due to corrupted data), `null` is returned.
 *
 * @param T The type of each element in the list. Must be serializable via
 *   kotlinx.serialization.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param serializer The [KSerializer] for the element type [T].
 * @param json The [Json] instance to use for serialization and deserialization.
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 * @param listSerializer The [KSerializer] for the list of type [T]. Defaults to
 *   [ListSerializer] of the provided element serializer.
 */
internal class NullableKSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    listSerializer: KSerializer<List<T>> = ListSerializer(serializer),
) : NullableCustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
    serializer = { json.encodeToString(listSerializer, it) },
    deserializer = { json.decodeFromString(listSerializer, it) },
    ioDispatcher = ioDispatcher,
)
