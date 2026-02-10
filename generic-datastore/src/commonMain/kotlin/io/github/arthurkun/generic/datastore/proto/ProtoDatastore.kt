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
}
