package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

/**
 * Internal interface that extracts read/write/remove operations from a [Preferences] snapshot
 * or [MutablePreferences] transaction, enabling batch operations across multiple preferences
 * in a single DataStore transaction.
 *
 * All sealed base preference classes implement this interface so that batch scopes
 * ([BatchReadScope], [BatchWriteScope], [BatchUpdateScope]) can read and write
 * values without each preference performing its own independent DataStore transaction.
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
