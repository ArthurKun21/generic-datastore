package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.DelegatedPreference

/**
 * Defines the contract for a Proto DataStore.
 *
 * This interface provides methods to access the proto message as a preference-like wrapper.
 *
 * @param T The proto message type.
 */
public interface ProtoDatastore<T> {
    /**
     * Returns the proto message wrapped as a [DelegatedPreference] instance.
     *
     * @return A [DelegatedPreference] instance for the proto message.
     */
    public fun data(): ProtoPreference<T>

    /**
     * Creates a [ProtoPreference] for an individual field of the proto message.
     *
     * The [getter] extracts the field value from a proto snapshot, and [updater] returns
     * a new proto with the field updated. This works for any nesting depth — for nested
     * fields, use `copy()` chains in [updater].
     *
     * ```kotlin
     * val namePref = protoDatastore.field(
     *     key = "name",
     *     defaultValue = "",
     *     getter = { it.name },
     *     updater = { proto, value -> proto.copy(name = value) },
     * )
     * ```
     *
     * @param F The field type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default value for the field.
     * @param getter A function that extracts the field from the proto snapshot.
     * @param updater A function that returns a new proto with the field updated.
     * @return A [ProtoPreference] for the individual field.
     */
    public fun <F> field(
        key: String,
        defaultValue: F,
        getter: (T) -> F,
        updater: (T, F) -> T,
    ): ProtoPreference<F>
}
