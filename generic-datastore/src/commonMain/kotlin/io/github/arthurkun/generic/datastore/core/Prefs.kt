package io.github.arthurkun.generic.datastore.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Extends the [Preference] interface to support property delegation.
 *
 * This interface allows a preference to be used as a delegated property,
 * simplifying its usage in classes.
 *
 * @param T The type of the preference value.
 */
interface Prefs<T> : ReadWriteProperty<Any?, T>, Preference<T> {
    /**
     * Resets the preference value to its default.
     */
    fun resetToDefault()
}

/**
 * Internal implementation of the [Prefs] interface.
 *
 * This class delegates the [Preference] functionalities to the provided [pref]
 * instance and implements the property delegation methods.
 *
 * @param T The type of the preference value.
 * @property pref The underlying [Preference] instance.
 */
internal class PrefsImpl<T>(
    private val pref: Preference<T>,
) : Prefs<T>,
    Preference<T> by pref {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = pref.getBlocking()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = pref.setBlocking(value)

    override fun resetToDefault() = pref.setBlocking(pref.defaultValue)
}
