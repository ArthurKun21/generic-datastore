package io.github.arthurkun.generic.datastore.preferences.core.data.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.utils.decodeEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [CustomSetGenericPreferenceItem] storing each enum constant of a [Set] by [Enum.name].
 *
 * [enumValues] supplies the constants used to decode stored names; names that no longer match any
 * constant are skipped when the set is read.
 */
internal class EnumSetPrimitive<T : Enum<T>>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Set<T>,
    enumValues: Array<T>,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { it.name },
    deserializer = { name ->
        decodeEnum(enumValues, name)
    },
    ioDispatcher = ioDispatcher,
)
