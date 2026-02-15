package io.github.arthurkun.generic.datastore.proto.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import kotlin.reflect.KProperty

/**
 * A [BasePreference] implementation backed by a Proto DataStore.
 *
 * Delegates all [BasePreference] operations to a [ProtoFieldPreference] with identity
 * getter/updater, and adds [io.github.arthurkun.generic.datastore.proto.ProtoPreference] (property delegation + resetToDefaultBlocking).
 *
 * @param T The proto message type.
 * @param datastore The [DataStore<T>] instance.
 * @param defaultValue The default value for the proto message.
 * @param key The key identifier for this preference.
 */
internal class GenericProtoPreferenceItem<T>(
    datastore: DataStore<T>,
    defaultValue: T,
    key: String = "proto_data",
) : ProtoPreference<T>,
    BasePreference<T> by ProtoFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        getter = { it },
        updater = { _, value -> value },
        defaultProtoValue = defaultValue,
    ) {

    override fun resetToDefaultBlocking() = setBlocking(defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setBlocking(value)
}
