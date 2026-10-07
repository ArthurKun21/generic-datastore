package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.utils.decodeEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [CustomGenericPreferenceItem] storing a single enum constant by [Enum.name].
 *
 * [enumValues] supplies the constants used to decode a stored name; names that no longer match any
 * constant fall back to [defaultValue].
 */
internal class EnumPrimitive<T : Enum<T>>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    enumValues: Array<T>,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = { name -> decodeEnum(enumValues, name) },
    ioDispatcher = ioDispatcher,
)
