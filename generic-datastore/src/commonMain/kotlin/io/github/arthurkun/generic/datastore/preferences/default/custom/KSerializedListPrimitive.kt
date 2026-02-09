package io.github.arthurkun.generic.datastore.preferences.default.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A [CustomGenericPreferenceItem] for storing a [List] of custom objects using
 * [kotlinx.serialization][KSerializer].
 *
 * The list is serialized to a JSON array string via [ListSerializer] and the provided
 * [json] instance, then stored as a string preference. If deserialization fails
 * (e.g., due to corrupted data), the [defaultValue] is returned.
 *
 * @param T The type of each element in the list. Must be serializable via
 *   kotlinx.serialization.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param defaultValue The default list returned when the preference is not set or
 *   deserialization fails.
 * @param serializer The [KSerializer] for the element type [T].
 * @param json The [Json] instance to use for serialization and deserialization.
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 */
internal class KSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: List<T>,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { json.encodeToString(ListSerializer(serializer), it) },
    deserializer = { json.decodeFromString(ListSerializer(serializer), it) },
    ioDispatcher = ioDispatcher,
)
