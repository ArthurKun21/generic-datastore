package io.github.arthurkun.generic.datastore.preferences.optional.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [NullableCustomSetGenericPreferenceItem] for a string-set-backed nullable custom [Set] using
 * caller-supplied element serializers.
 */
internal class NullableSerializedSetPrimitive<T : Any>(
    datastore: DataStore<Preferences>,
    key: String,
    serializer: (T) -> String,
    deserializer: (String) -> T,
    onDecodeFailure: ((String, Throwable) -> Unit)? = null,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = serializer,
    deserializer = deserializer,
    onDecodeFailure = onDecodeFailure,
    ioDispatcher = ioDispatcher,
)
