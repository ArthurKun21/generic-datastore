package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeList
import io.github.arthurkun.generic.datastore.preferences.utils.serializeList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [NullableCustomGenericPreferenceItem] that stores a nullable [List] inside one JSON array
 * string using caller-supplied element serializers.
 */
internal class NullableSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    elementSerializer: (T) -> String,
    elementDeserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
    serializer = { list -> serializeList(list, elementSerializer) },
    deserializer = { str -> deserializeList(str, elementDeserializer) },
    ioDispatcher = ioDispatcher,
)
