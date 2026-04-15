package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [CustomGenericPreferenceItem] for a single string-backed custom value using caller-supplied
 * serializers.
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
