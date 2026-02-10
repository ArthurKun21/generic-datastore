@file:Suppress("UNCHECKED_CAST")

package io.github.arthurkun.generic.datastore.preferences.batch

import io.github.arthurkun.generic.datastore.preferences.Preferences
import androidx.datastore.preferences.core.Preferences as DataStorePreferences

/**
 * Scope for batch-reading multiple preferences from a single [DataStorePreferences] snapshot.
 *
 * Instead of each preference independently accessing the DataStore, all reads within this
 * scope share the same snapshot, eliminating redundant transactions.
 *
 * Use [get] or the indexing operator (`this[pref]`) to read preference values.
 *
 * Obtain this scope from [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchGet]
 * or [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchReadFlow].
 */
public class BatchReadScope internal constructor(
    private val snapshot: DataStorePreferences,
) {
    /**
     * Reads the given preference's current value from the shared snapshot.
     *
     * @param preference The preference to read.
     * @return The preference value, or its default if the key is absent.
     * @throws IllegalStateException if [preference] does not implement [PreferencesAccessor].
     */
    public operator fun <T> get(preference: Preferences<T>): T {
        val accessible = preference as? PreferencesAccessor<T>
            ?: error("Batch operations only support preferences created by this library")
        return accessible.readFrom(snapshot)
    }
}
