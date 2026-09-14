package io.github.arthurkun.generic.datastore.core

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Minimal [DataStore] implementation that keeps the current value in memory.
 *
 * Backs the library's in-memory datastore factories for tests and previews. It does not support
 * migrations or corruption handling (there is no file to corrupt); writes are serialized with a
 * [Mutex] and an update whose [transform] throws leaves the stored value untouched.
 *
 * @param T The stored value type.
 * @param initialValue The value exposed before the first successful write.
 */
@InternalGenericDatastoreApi
public class InMemoryDataStore<T>(initialValue: T) : DataStore<T> {

    private val state = MutableStateFlow(initialValue)

    private val mutex = Mutex()

    override val data: Flow<T> = state.asStateFlow()

    override suspend fun updateData(transform: suspend (T) -> T): T = mutex.withLock {
        val newValue = transform(state.value)
        state.value = newValue
        newValue
    }
}
