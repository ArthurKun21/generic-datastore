package io.github.arthurkun.generic.datastore.preferences.core.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A [CustomSetGenericPreferenceItem] for storing a [Set] of custom objects using
 * [kotlinx.serialization][KSerializer].
 *
 * Each element is serialized to a JSON String and stored using [stringSetPreferencesKey].
 * On retrieval, each String element is deserialized back via the provided [KSerializer].
 *
 * If deserialization of an individual element fails, that element is skipped.
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
internal class KSerializedSetPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Set<T>,
    serializer: KSerializer<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { json.encodeToString(serializer, it) },
    deserializer = { json.decodeFromString(serializer, it) },
    ioDispatcher = ioDispatcher,
)
