package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.Preferences

/**
 * The values of every preference in a [PreferenceBatch], read from one consistent
 * [Preferences] snapshot.
 *
 * Access values with the typed indexing operator and a [BatchPref] handle, or convert the whole
 * batch with [toMap]:
 *
 * ```kotlin
 * val values = datastore.batchRead(settings)
 * val volume: Int = values[volumePref]
 * val all: Map<BatchPref<*>, Any?> = values.toMap()
 * ```
 */
public class BatchValues internal constructor(
    preferences: Preferences,
    batch: PreferenceBatch,
) {

    private val values: Map<BatchPref<*>, Any?> =
        batch.associateWith { pref -> pref.readFrom(preferences) }

    /**
     * Returns the value of [pref] from the snapshot, or its default when the key is absent.
     *
     * @param T The preference value type.
     * @param pref A preference declared in the batch this snapshot was built from.
     * @throws IllegalStateException If [pref] is not part of the batch.
     */
    public operator fun <T> get(pref: BatchPref<T>): T {
        if (!values.containsKey(pref)) {
            error("Preference '${pref.key}' is not part of this batch.")
        }
        @Suppress("UNCHECKED_CAST")
        return values[pref] as T
    }

    /**
     * Returns every declared preference mapped to its stored value (or default), in declaration
     * order.
     */
    public fun toMap(): Map<BatchPref<*>, Any?> = values

    override fun equals(other: Any?): Boolean = other is BatchValues && other.values == values

    override fun hashCode(): Int = values.hashCode()

    override fun toString(): String = "BatchValues(values=$values)"
}
