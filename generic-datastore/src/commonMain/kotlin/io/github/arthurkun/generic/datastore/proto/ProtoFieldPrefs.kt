package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.proto.core.ProtoFieldPreference
import kotlin.reflect.KProperty

/**
 * Wraps a [io.github.arthurkun.generic.datastore.proto.core.ProtoFieldPreference] to satisfy the [ProtoPreference] / [DelegatedPreference]
 * contract, adding property delegation and [resetToDefaultBlocking].
 *
 * @param P The proto/data class type.
 * @param T The field type.
 * @param pref The underlying [io.github.arthurkun.generic.datastore.proto.core.ProtoFieldPreference] to delegate to.
 */
internal class ProtoFieldPrefs<P, T>(
    private val pref: ProtoFieldPreference<P, T>,
) : ProtoPreference<T>, BasePreference<T> by pref {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = pref.setBlocking(value)

    override fun resetToDefaultBlocking() = pref.setBlocking(pref.defaultValue)
}
