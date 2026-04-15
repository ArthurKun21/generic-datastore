package io.github.arthurkun.generic.datastore.preferences.core.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [CustomSetGenericPreferenceItem] for a string-set-backed custom [Set] using caller-supplied
 * element serializers.
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
