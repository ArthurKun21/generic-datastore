@file:Suppress("UNCHECKED_CAST")

package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences
import io.github.arthurkun.generic.datastore.preferences.Preference

/**
 * Scope for atomically reading and writing multiple preferences in a single DataStore transaction.
 *
 * Reads and writes operate on the same [MutablePreferences] transaction state, so reads
 * reflect writes that happened earlier in the same block.
 *
 * Use [get]/[set] or indexing operators (`this[pref]`, `this[pref] = value`) to access preferences.
 *
 * Obtain this scope from [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchUpdate].
 */
public class BatchUpdateScope internal constructor(
    private val mutablePreferences: MutablePreferences,
) {
    /**
     * Reads the given preference's current value from the ongoing transaction state.
     *
     * @param preference The preference to read.
     * @return The preference value, or its default if the key is absent.
     * @throws IllegalStateException if [preference] does not implement [PreferencesAccessor].
     */
    public operator fun <T> get(preference: Preference<T>): T {
        val accessible = preference as? PreferencesAccessor<T>
            ?: error("Batch operations only support preferences created by this library")
        return accessible.readFrom(mutablePreferences)
    }

    /**
     * Sets the given preference's value in the shared transaction.
     *
     * @param preference The preference to write.
     * @param value The new value to write.
     * @throws IllegalStateException if [preference] does not implement [PreferencesAccessor].
     */
    public operator fun <T> set(preference: Preference<T>, value: T) {
        val accessible = preference as? PreferencesAccessor<T>
            ?: error("Batch operations only support preferences created by this library")
        accessible.writeInto(mutablePreferences, value)
    }

    /**
     * Reads the current value, applies [transform], and writes back the result.
     *
     * @param preference The preference to update.
     * @param transform A function that receives the current value and returns the new value.
     */
    public fun <T> update(preference: Preference<T>, transform: (T) -> T) {
        set(preference, transform(get(preference)))
    }

    /**
     * Removes the given preference's key from the shared transaction.
     *
     * @param preference The preference to remove.
     * @throws IllegalStateException if [preference] does not implement [PreferencesAccessor].
     */
    public fun <T> delete(preference: Preference<T>) {
        val accessible = preference as? PreferencesAccessor<T>
            ?: error("Batch operations only support preferences created by this library")
        accessible.removeFrom(mutablePreferences)
    }

    /**
     * Resets the given preference to its [Preference.defaultValue] in the shared transaction.
     *
     * @param preference The preference to reset.
     */
    public fun <T> resetToDefault(preference: Preference<T>) {
        set(preference, preference.defaultValue)
    }
}
