package io.github.arthurkun.generic.datastore.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Returns a [Flow] that emits the preference value only when it changes,
 * filtering out consecutive duplicate emissions.
 *
 * This is a convenience wrapper around `asFlow().distinctUntilChanged()`.
 *
 * @param T The type of the preference value.
 * @return A [Flow] that emits distinct consecutive values.
 */
public fun <T> DelegatedPreference<T>.distinctFlow(): Flow<T> = asFlow().distinctUntilChanged()
