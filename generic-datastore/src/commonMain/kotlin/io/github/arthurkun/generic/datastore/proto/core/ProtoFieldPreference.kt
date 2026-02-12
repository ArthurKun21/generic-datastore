package io.github.arthurkun.generic.datastore.proto.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * A [BasePreference] implementation for an individual field within a Proto DataStore.
 *
 * @param P The proto/data class type.
 * @param T The field type.
 * @param datastore The [DataStore] instance holding the proto.
 * @param key A unique key identifying this field preference.
 * @param defaultValue The default value for the field.
 * @param getter A function that extracts the field from the proto snapshot.
 * @param updater A function that returns a new proto with the field updated.
 * @param defaultProtoValue The default proto value, used as fallback on [IOException].
 * @param ioDispatcher The dispatcher for IO operations.
 */
internal class ProtoFieldPreference<P, T>(
    datastore: DataStore<P>,
    key: String,
    defaultValue: T,
    getter: (P) -> T,
    updater: (P, T) -> P,
    defaultProtoValue: P,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProtoSerialFieldPreference<P, T>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        getter = getter,
        updater = updater,
        defaultProtoValue = defaultProtoValue,
        ioDispatcher = ioDispatcher,
    )
