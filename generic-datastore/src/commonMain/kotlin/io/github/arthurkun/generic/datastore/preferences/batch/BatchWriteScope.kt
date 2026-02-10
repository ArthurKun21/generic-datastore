package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences
import io.github.arthurkun.generic.datastore.preferences.Preferences

/**
 * Scope for batch-writing multiple preferences in a single DataStore `edit` transaction.
 *
 * All [set], [delete], and [resetToDefault] calls within this scope write into the
 * same [MutablePreferences] instance, collapsing N writes into one atomic transaction.
 *
 * Use [set] or the indexing operator (`this[pref] = value`) to write preference values.
 *
 * Obtain this scope from [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchWrite].
 */
public class BatchWriteScope internal constructor(
    private val mutablePreferences: MutablePreferences,
) {
    /**
     * Sets the given preference's value in the shared transaction.
     *
     * @param preference The preference to write.
     * @param value The new value to write.
     * @throws IllegalStateException if [preference] does not implement [PreferencesAccessor].
     */
    public operator fun <T> set(preference: Preferences<T>, value: T) {
        val accessible = preference as? PreferencesAccessor<T>
            ?: error("Batch operations only support preferences created by this library")
        accessible.writeInto(mutablePreferences, value)
    }

    /**
     * Removes the given preference's key from the shared transaction.
     *
     * @param preference The preference to remove.
     * @throws IllegalStateException if [preference] does not implement [PreferencesAccessor].
     */
    public fun <T> delete(preference: Preferences<T>) {
        val accessible = preference as? PreferencesAccessor<T>
            ?: error("Batch operations only support preferences created by this library")
        accessible.removeFrom(mutablePreferences)
    }

    /**
     * Resets the given preference to its [Preferences.defaultValue] in the shared transaction.
     *
     * @param preference The preference to reset.
     */
    public fun <T> resetToDefault(preference: Preferences<T>) {
        set(preference, preference.defaultValue)
    }
}
