package io.github.arthurkun.generic.datastore.preferences.core.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * A [CustomSetGenericPreferenceItem] for storing a [Set] of custom objects using per-element serialization.
 *  Each element is serialized to a String via [serializer] and stored using
 *  [stringSetPreferencesKey]. On retrieval, each String element is deserialized back
 *  via [deserializer].
 *
 * If deserialization fails (e.g., due to corrupted data), the [defaultValue] is returned.
 *
 * @param T The type of the custom object.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param defaultValue The default value returned when the preference is not set or
 *   deserialization fails.
 * @param serializer A function to serialize the object of type [T] to its [String]
 *   representation for storage.
 * @param deserializer A function to deserialize the [String] representation back to an
 *   object of type [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 */
internal class SerializedSetPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Set<T>,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)
