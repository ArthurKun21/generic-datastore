package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

/**
 * Internal contract that exposes snapshot and transaction access for a preference.
 *
 * Batch scopes use this interface to read and write values without each preference performing its
 * own independent datastore operation.
 *
 * @param T The type of the preference value.
 */
internal interface PreferencesAccessor<T> {
    /**
     * Reads this preference's value from a [Preferences] snapshot.
     *
     * @param preferences The immutable preferences snapshot to read from.
     * @return The preference value, or the default value if absent.
     */
    fun readFrom(preferences: Preferences): T

    /**
     * Writes this preference's value into [MutablePreferences].
     *
     * @param mutablePreferences The mutable preferences to write into.
     * @param value The value to write.
     */
    fun writeInto(mutablePreferences: MutablePreferences, value: T)

    /**
     * Removes this preference's key from [MutablePreferences].
     *
     * @param mutablePreferences The mutable preferences to remove from.
     */
    fun removeFrom(mutablePreferences: MutablePreferences)
}
