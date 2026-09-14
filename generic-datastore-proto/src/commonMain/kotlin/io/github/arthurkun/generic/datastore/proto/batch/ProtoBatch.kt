package io.github.arthurkun.generic.datastore.proto.batch

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference

/**
 * Marks the proto batch DSL scopes.
 */
@DslMarker
public annotation class ProtoBatchDsl

/**
 * Scope for atomically writing multiple proto fields in a single `updateData` transaction.
 *
 * Every [set] applies its change to the in-progress proto snapshot; the final snapshot is written
 * to disk once, so N field writes collapse into a single transaction instead of N:
 *
 * ```kotlin
 * protoDatastore.batchWrite {
 *     set(nameField, "Alice")
 *     set(ageField, 30)
 *     resetToDefault(nicknameField)
 * }
 * ```
 *
 * Writes are applied in declaration order — later sets observe and replace earlier ones.
 * Setting a `data()` preference replaces the whole proto message with the given value.
 *
 * @param P The proto message type.
 */
@ProtoBatchDsl
public open class ProtoWriteScope<P> internal constructor(
    private val datastore: DataStore<P>,
    protected var proto: P,
) {
    /**
     * Writes [value] into the in-progress proto snapshot.
     *
     * @param F The field value type.
     * @param pref The field preference to write.
     * @param value The new value for the field.
     * @throws IllegalArgumentException If [pref] was not created by this library or belongs to a
     *   different datastore.
     */
    public operator fun <F> set(pref: ProtoPreference<F>, value: F) {
        proto = accessor(pref).writeInto(proto, value)
    }

    /**
     * Resets the given field to its [ProtoPreference.defaultValue] in the in-progress snapshot.
     *
     * @param F The field value type.
     * @param pref The field preference to reset.
     * @throws IllegalArgumentException If [pref] was not created by this library or belongs to a
     *   different datastore.
     */
    public fun <F> resetToDefault(pref: ProtoPreference<F>) {
        set(pref, pref.defaultValue)
    }

    internal fun currentProto(): P = proto

    private fun <F> accessor(pref: ProtoPreference<F>): ProtoAccessor<P, F> =
        protoAccessor(datastore, pref)
}

@Suppress("UNCHECKED_CAST")
internal fun <P, F> protoAccessor(datastore: DataStore<P>, pref: ProtoPreference<F>): ProtoAccessor<P, F> {
    val accessor = pref as? ProtoAccessor<*, *>
        ?: throw IllegalArgumentException(
            "Batch operations only support preferences created by this library",
        )
    require(accessor.boundDatastore === datastore) {
        "Preference belongs to a different datastore"
    }
    return accessor as ProtoAccessor<P, F>
}

/**
 * Scope for atomically reading and writing multiple proto fields in a single `updateData`
 * transaction.
 *
 * Reads observe changes made earlier in the same block, so multi-field derivations stay
 * consistent and still land as one disk write:
 *
 * ```kotlin
 * protoDatastore.batchUpdate {
 *     update(counterField) { it + 1 }
 *     set(lastChangedField, clock.now())
 * }
 * ```
 *
 * @param P The proto message type.
 */
@ProtoBatchDsl
public class ProtoUpdateScope<P> internal constructor(
    private val datastore: DataStore<P>,
    proto: P,
) : ProtoWriteScope<P>(datastore, proto) {
    /**
     * Reads the given field's current value from the in-progress proto snapshot, including any
     * writes made earlier in the block.
     *
     * @param F The field value type.
     * @param pref The field preference to read.
     * @throws IllegalArgumentException If [pref] was not created by this library or belongs to a
     *   different datastore.
     */
    public operator fun <F> get(pref: ProtoPreference<F>): F = accessor(pref).readFrom(proto)

    /**
     * Reads the current value, applies [transform], and writes the result back.
     *
     * @param F The field value type.
     * @param pref The field preference to update.
     * @param transform A function that receives the current value and returns the new value.
     * @throws IllegalArgumentException If [pref] was not created by this library or belongs to a
     *   different datastore.
     */
    public fun <F> update(pref: ProtoPreference<F>, transform: (F) -> F) {
        set(pref, transform(get(pref)))
    }

    private fun <F> accessor(pref: ProtoPreference<F>): ProtoAccessor<P, F> =
        protoAccessor(datastore, pref)
}
