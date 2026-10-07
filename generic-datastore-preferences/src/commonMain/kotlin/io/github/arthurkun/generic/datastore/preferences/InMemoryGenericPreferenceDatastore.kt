package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Internal [DataStore<Preferences>] implementation that keeps every value in memory.
 *
 * All reads observe a [MutableStateFlow] snapshot and all writes run inside a [Mutex], so
 * concurrent [updateData] transforms apply sequentially and each transform observes the
 * previous result — the same guarantee the file-backed DataStore actor provides. A transform
 * that throws propagates the exception and leaves the stored state unchanged.
 *
 * Nothing is ever persisted: values live for as long as the owning
 * [GenericPreferencesDatastore] instance.
 * Used by the `*InMemory` preference factories; the file-backed primitive classes operate on
 * it unchanged.
 *
 * @property initial The [Preferences] snapshot the store starts with.
 */
internal class InMemoryGenericPreferenceDatastore(
    initial: Preferences = emptyPreferences(),
) : DataStore<Preferences> {

    private val state = MutableStateFlow(initial)

    private val mutex = Mutex()

    override val data: Flow<Preferences> = state.asStateFlow()

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences = mutex.withLock {
        val updated = transform(state.value)
        state.value = updated
        updated
    }
}
