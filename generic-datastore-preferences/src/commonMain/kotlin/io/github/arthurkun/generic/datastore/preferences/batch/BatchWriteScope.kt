package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.MutablePreferences

/**
 * Unified scope for batch-writing multiple preferences in a single DataStore `edit` transaction.
 *
 * This scope extends [PrefBuilder], so one `datastore.batchWrite { … }` block both **declares**
 * preferences and **operates** on them — reusing existing preferences or declaring from scratch:
 *
 * ```kotlin
 * val text = datastore.string("text", "Hello World!")
 *
 * datastore.batchWrite {
 *     val textHandle = add(text)          // reuse an existing Preference
 *     val numHandle = int("num", 0)       // …or declare inline
 *     set(textHandle, "Hi")
 *     set(numHandle, 42)
 *     resetToDefault(textHandle)
 * }
 * ```
 *
 * All [set], [delete], and [resetToDefault] calls within this scope write into the same
 * [MutablePreferences] instance, collapsing many logical writes into one atomic transaction.
 * Batch membership is not enforced: any [BatchPref] handle may be written, so
 * `batchWrite { set(handle, value) }` works without re-declaring.
 *
 * Obtain this scope from
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchWrite].
 */
@PreferencesBatchDsl
public class BatchWriteScope internal constructor(
    private val mutablePreferences: MutablePreferences,
) : PrefBuilder() {
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
