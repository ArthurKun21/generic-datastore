package io.github.arthurkun.generic.datastore.preferences.optional.data.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.utils.decodeEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [NullableCustomGenericPreferenceItem] storing a single enum constant by [Enum.name].
 *
 * [enumValues] supplies the constants used to decode a stored name; missing keys and names that no
 * longer match any constant both read back as `null`.
 */
internal class NullableEnumPrimitive<T : Enum<T>>(
    datastore: DataStore<Preferences>,
    key: String,
    enumValues: Array<T>,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = { it.name },
    deserializer = { name ->
        decodeEnum(enumValues, name)
    },
    ioDispatcher = ioDispatcher,
)
