package io.github.arthurkun.generic.datastore.core

import kotlin.properties.ReadWriteProperty

/**
 * Extends the [BasePreference] interface to support property delegation.
 *
 * This interface allows a preference to be used as a delegated property,
 * simplifying its usage in classes.
 *
 * @param T The type of the preference value.
 */
public interface DelegatedPreference<T> : ReadWriteProperty<Any?, T>, BasePreference<T> {
    /**
     * Resets the preference value to its default.
     */
    public fun resetToDefaultBlocking()
}
