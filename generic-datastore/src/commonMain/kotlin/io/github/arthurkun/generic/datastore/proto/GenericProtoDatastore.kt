package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.Prefs

/**
 * A DataStore implementation for Proto DataStore.
 *
 * This class wraps a [DataStore<T>] instance for typed proto messages.
 *
 * @param T The proto message type.
 * @property datastore The underlying [DataStore<T>] instance.
 * @property defaultValue The default value for the proto message.
 */
@Suppress("unused")
class GenericProtoDatastore<T>(
    private val datastore: DataStore<T>,
    private val defaultValue: T,
    private val key: String = "proto_datastore",
) : ProtoDatastore<T> {

    override fun data(): Prefs<T> {
        return ProtoPreference(
            datastore = datastore,
            defaultValue = defaultValue,
            key = key,
        )
    }
}
