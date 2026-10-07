package io.github.arthurkun.generic.datastore.preferences.optional.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.utils.decodeEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * [NullableSetGenericPreferenceItem] storing each enum constant of a [Set] by [Enum.name].
 *
 * [enumValues] supplies the constants used to decode stored names; names that no longer match any
 * constant are skipped when the set is read, so an entry with no surviving constants reads back as
 * an empty set. Only a missing key reads back as `null`.
 */
internal class NullableEnumSetPrimitive<T : Enum<T>>(
    datastore: DataStore<Preferences>,
    key: String,
    enumValues: Array<T>,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableSetGenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    serializer = { it.name },
    deserializer = { name ->
        decodeEnum(enumValues, name)
    },
    ioDispatcher = ioDispatcher,
)
