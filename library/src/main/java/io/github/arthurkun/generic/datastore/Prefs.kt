package io.github.arthurkun.generic.datastore

import kotlin.reflect.KProperty

/**
 * Extends the [Preference] interface to support property delegation.
 *
 * This interface allows a preference to be used as a delegated property,
 * simplifying its usage in classes.
 *
 * @param T The type of the preference value.
 */
interface Prefs<T> : Preference<T> {
    /**
     * Gets the value of the preference for property delegation.
     *
     * @param thisRef The object that owns the delegated property.
     * @param property The metadata of the delegated property.
     * @return The current preference value.
     */
    suspend fun getValue(thisRef: Any, property: KProperty<*>): T

    /**
     * Sets the value of the preference for property delegation.
     *
     * @param thisRef The object that owns the delegated property.
     * @param property The metadata of the delegated property.
     * @param value The new value for the preference.
     */
    suspend fun setValue(thisRef: Any, property: KProperty<*>, value: T)

    /**
     * Resets the preference value to its default.
     */
    suspend fun resetToDefault()
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
internal class PrefsImpl<T>(private val pref: Preference<T>) :
    Prefs<T>,
    Preference<T> by pref {
    override suspend fun getValue(thisRef: Any, property: KProperty<*>) = pref.get()

    override suspend fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        pref.set(value)

    override suspend fun resetToDefault() = pref.set(pref.defaultValue())
}