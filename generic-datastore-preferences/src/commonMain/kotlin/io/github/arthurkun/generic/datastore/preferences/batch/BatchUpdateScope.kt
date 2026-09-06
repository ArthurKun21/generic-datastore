package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences

/**
 * Scope for atomically reading and writing multiple preferences in a single DataStore transaction.
 *
 * Reads and writes operate on the same [MutablePreferences] transaction state, so reads reflect
 * writes that happened earlier in the same block.
 *
 * Use [get]/[set] or the indexing operators (`this[pref]`, `this[pref] = value`) to access
 * preferences. Obtain this scope from
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchUpdate].
 *
 * Example:
 * ```kotlin
 * datastore.batchUpdate(batch) {
 *     this[counter] = this[counter] + 1
 * }
 * ```
 */
@PreferencesBatchDsl
public class BatchUpdateScope internal constructor(
    private val mutablePreferences: MutablePreferences,
) {
    /**
     * Reads the given preference's current value from the ongoing transaction state.
     *
     * @param T The preference value type.
     * @param pref The preference to read.
     * @return The preference value, or its default if the key is absent.
     */
    public operator fun <T> get(pref: BatchPref<T>): T = pref.readFrom(mutablePreferences)

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
     * Reads the current value, applies [transform], and writes back the result.
     *
     * @param T The preference value type.
     * @param pref The preference to update.
     * @param transform A function that receives the current value and returns the new value.
     */
    public fun <T> update(pref: BatchPref<T>, transform: (T) -> T) {
        set(pref, transform(get(pref)))
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
