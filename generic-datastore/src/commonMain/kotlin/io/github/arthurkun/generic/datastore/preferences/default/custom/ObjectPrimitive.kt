package io.github.arthurkun.generic.datastore.preferences.default.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * A [io.github.arthurkun.generic.datastore.preferences.default.GenericPreferenceItem] for storing custom [Object] values.
 * This class handles the serialization of the object to a String for storage
 * and deserialization from String back to the object on retrieval.
 *
 * @param T The type of the custom object.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set or an error occurs during deserialization.
 * @param serializer A function to serialize the object of type [T] to its String representation for storage.
 * @param deserializer A function to deserialize the String representation back to an object of type [T].
 */
internal class ObjectPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = serializer,
    deserializer = deserializer,
    ioDispatcher = ioDispatcher,
)
