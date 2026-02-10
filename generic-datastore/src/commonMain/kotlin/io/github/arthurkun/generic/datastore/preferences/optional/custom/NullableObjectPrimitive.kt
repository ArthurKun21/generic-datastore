package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * A [NullableCustomGenericPreferenceItem] for storing nullable custom objects using
 * caller-provided serialization and deserialization functions.
 *
 * If deserialization fails (e.g., due to corrupted data), `null` is returned.
 *
 * @param T The non-null type of the custom object.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param serializer A function to serialize the object of type [T] to its [String]
 *   representation for storage.
 * @param deserializer A function to deserialize the [String] representation back to an
 *   object of type [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 */
internal class NullableObjectPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)
