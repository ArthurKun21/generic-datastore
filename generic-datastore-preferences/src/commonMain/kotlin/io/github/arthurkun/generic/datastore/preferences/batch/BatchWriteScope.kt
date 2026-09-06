package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences

/**
 * Scope for batch-writing multiple preferences in a single DataStore `edit` transaction.
 *
 * All [set], [delete], and [resetToDefault] calls within this scope write into the same
 * [MutablePreferences] instance, collapsing many logical writes into one atomic transaction.
 *
 * Use [set] or the indexing operator (`this[pref] = value`) to write preference values.
 * Obtain this scope from
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchWrite].
 *
 * Example:
 * ```kotlin
 * datastore.batchWrite(batch) {
 *     this[username] = "rafael"
 *     resetToDefault(hasSeenOnboarding)
 * }
 * ```
 */
@PreferencesBatchDsl
public class BatchWriteScope internal constructor(
    private val mutablePreferences: MutablePreferences,
) {
    /**
     * Sets the given preference's value in the shared transaction. Writing `null` to a nullable
     * preference removes its key.
     *
     * @param T The preference value type.
     * @param pref The preference to write.
     * @param value The new value to write.
     */
    public operator fun <T> set(pref: BatchPref<T>, value: T) {
        pref.writeTo(mutablePreferences, value)
    }

    /**
     * Removes the given preference's key from the shared transaction.
     *
     * @param T The preference value type.
     * @param pref The preference to remove.
     */
    public fun <T> delete(pref: BatchPref<T>) {
        pref.removeFrom(mutablePreferences)
    }

    /**
     * Resets the given preference to its [BatchPref.defaultValue] in the shared transaction.
     *
     * @param T The preference value type.
     * @param pref The preference to reset.
     */
    public fun <T> resetToDefault(pref: BatchPref<T>) {
        set(pref, pref.defaultValue)
    }
}
