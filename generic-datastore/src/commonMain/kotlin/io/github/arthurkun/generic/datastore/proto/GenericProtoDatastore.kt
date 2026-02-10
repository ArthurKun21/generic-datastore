@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore

/**
 * A DataStore implementation for Proto DataStore.
 *
 * This class wraps a [DataStore<T>] instance for typed proto messages.
 *
 * @param T The proto message type.
 * @param datastore The underlying [DataStore<T>] instance.
 * @param defaultValue The default value for the proto message.
 */
public class GenericProtoDatastore<T>(
    internal val datastore: DataStore<T>,
    private val defaultValue: T,
    private val key: String = "proto_datastore",
) : ProtoDatastore<T> {

    override fun data(): ProtoPreference<T> {
        return GenericProtoPreferenceItem(
            datastore = datastore,
            defaultValue = defaultValue,
            key = key,
        )
    }
}
