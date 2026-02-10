package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.DelegatedPreference

/**
 * A marker interface for [io.github.arthurkun.generic.datastore.core.DelegatedPreference] instances backed by a Proto DataStore.
 *
 * This interface restricts certain extension functions
 * to proto-backed implementations only, preventing their use with Preferences DataStore.
 *
 * @param T The type of the proto data class value.
 */
public interface ProtoPreference<T> : DelegatedPreference<T>
