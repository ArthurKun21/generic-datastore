package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences

/**
 * Unified scope for atomically reading and writing multiple preferences in a single DataStore
 * transaction.
 *
 * This scope extends [PrefBuilder], so one `datastore.batchUpdate { … }` block both **declares**
 * preferences and **operates** on them — reusing existing preferences or declaring from scratch:
 *
 * ```kotlin
 * datastore.batchUpdate {
 *     val counter = add(counterPref) // …or: val counter = int("counter", 0)
 *     this[counter] = this[counter] + 1
 * }
 * ```
 *
 * Reads and writes operate on the same [MutablePreferences] transaction state, so reads reflect
 * writes that happened earlier in the same block. Batch membership is not enforced: any
 * [BatchPref] handle may be read or written.
 *
 * Obtain this scope from
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchUpdate].
 */
@PreferencesBatchDsl
public class BatchUpdateScope internal constructor(
    private val mutablePreferences: MutablePreferences,
) : PrefBuilder() {
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
