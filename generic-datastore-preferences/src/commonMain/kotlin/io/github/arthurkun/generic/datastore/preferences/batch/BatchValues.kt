package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.preferences.core.Preferences

/**
 * The values of every preference in a [PreferenceBatch], read from one consistent
 * [Preferences] snapshot.
 *
 * Access values with the typed indexing operator and a [BatchPref] handle, convert the whole
 * batch with [toMap] or [toList], or destructure positionally in declaration order:
 *
 * ```kotlin
 * val values = datastore.batchReadValues {
 *     add(volumePref)
 *     add(namePref)
 * }
 * val volume: Int = values[volumePref]
 * val all: Map<BatchPref<*>, Any?> = values.toMap()
 * val (volumeDestructured: Int, nameDestructured: String) = values
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
     *
     * The returned map is a defensive copy: mutating it does not affect this snapshot.
     */
    public fun toMap(): Map<BatchPref<*>, Any?> = values.toMap()

    /**
     * Returns every value in declaration order.
     */
    public fun toList(): List<Any?> = values.values.toList()

    /**
     * Positional destructuring access in declaration order, e.g.
     * `val (text: String, num: Int) = values` or
     * `flow.collect { (text: String, num: Int) -> … }`.
     *
     * These casts are unchecked: the caller must destructure in the same order and with the same
     * types as declared. Prefer the type-safe [get] with the declared [BatchPref] handles when
     * the order is not obvious at the call site.
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component1(): T = toList()[0] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component2(): T = toList()[1] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component3(): T = toList()[2] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component4(): T = toList()[3] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component5(): T = toList()[4] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component6(): T = toList()[5] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component7(): T = toList()[6] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component8(): T = toList()[7] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component9(): T = toList()[8] as T

    /** See [component1]. */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> component10(): T = toList()[9] as T

    override fun equals(other: Any?): Boolean = other is BatchValues && other.values == values

    override fun hashCode(): Int = values.hashCode()

    override fun toString(): String = "BatchValues(values=$values)"
}
