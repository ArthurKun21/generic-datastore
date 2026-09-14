package io.github.arthurkun.generic.datastore.proto

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.proto.batch.ProtoAccessor
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlin.reflect.KProperty

/**
 * Wraps a [io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference] to satisfy the [ProtoPreference] / [DelegatedPreference]
 * contract, adding property delegation, [resetToDefaultBlocking], and the internal batch accessor.
 *
 * @param P The proto/data class type.
 * @param T The field type.
 * @param pref The underlying [io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference] to delegate to.
 */
internal class ProtoFieldPrefs<P, T>(
    internal val pref: ProtoSerialFieldPreference<P, T>,
) : ProtoPreference<T>, BasePreference<T> by pref, ProtoAccessor<P, T> {

    override val boundDatastore: DataStore<P> get() = pref.datastore

    override fun readFrom(proto: P): T = pref.getter(proto)

    override fun writeInto(proto: P, value: T): P = pref.updater(proto, value)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit = pref.setBlocking(value)

    override fun resetToDefaultBlocking(): Unit = pref.setBlocking(pref.defaultValue)
}
