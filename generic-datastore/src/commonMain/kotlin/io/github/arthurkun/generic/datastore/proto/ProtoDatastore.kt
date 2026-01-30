package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.Prefs

/**
 * Defines the contract for a Proto DataStore.
 *
 * This interface provides methods to access the proto message as a preference-like wrapper.
 *
 * @param T The proto message type.
 */
interface ProtoDatastore<T> {
    /**
     * Returns the proto message wrapped as a [Prefs] instance.
     *
     * @return A [Prefs] instance for the proto message.
     */
    fun data(): Prefs<T>
}
