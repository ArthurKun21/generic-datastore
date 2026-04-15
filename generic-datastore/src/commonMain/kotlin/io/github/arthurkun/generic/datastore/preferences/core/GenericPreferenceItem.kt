package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.batch.PreferencesAccessor
import io.github.arthurkun.generic.datastore.preferences.utils.dataOrEmpty
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Base implementation for non-null Preferences DataStore entries.
 *
 * Subclasses bind a concrete [Preferences.Key] and inherit the common read, write, delete,
 * blocking, flow, and batch-access behavior used by primitive preferences.
 *
 * Missing keys read back as [defaultValue].
 *
 * @param T The stored value type.
 * @property datastore The [DataStore<Preferences>] instance used for storage.
 * @property key The unique preference key name.
 * @property defaultValue The value returned when the key is not present.
 * @property preferences The typed [Preferences.Key] used to access this value in DataStore.
 */
internal sealed class GenericPreferenceItem<T>(
    internal val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val preferences: Preferences.Key<T>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BasePreference<T>, PreferencesAccessor<T> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    /**
     * Returns the unique String key used to identify this preference within the DataStore.
     */
    override fun key(): String = key

    /**
     * Retrieves the current value of the preference from DataStore.
     * If the key is not found in DataStore or an error occurs during retrieval,
     * this function returns the [defaultValue]. This is a suspending function.
     */
    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    /**
     * Sets the value of the preference in the DataStore.
     * This is a suspending function.
     * @param value The new value to store for this preference.
     */
    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds[preferences] = value
            }
        }
    }

    /**
     * Atomically reads the current value and applies [transform] to compute a new value,
     * then writes it back in a single [datastore.edit] transaction.
     */
    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                val current = ds[preferences] ?: defaultValue
                ds[preferences] = transform(current)
            }
        }
    }

    /**
     * Removes the preference from the DataStore.
     * This is a suspending function.
     */
    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds.remove(preferences)
            }
        }
    }

    override suspend fun resetToDefault() = set(defaultValue)

    /**
     * Returns a [Flow] that emits the preference's current value and subsequent updates from DataStore.
     * If the preference is not set in the DataStore or an error occurs during retrieval,
     * the flow will emit the [defaultValue].
     */
    override fun asFlow(): Flow<T> {
        return datastore
            .dataOrEmpty
            .map { preferences ->
                preferences[this.preferences] ?: defaultValue
            }
    }

    /**
     * Converts the preference [Flow] into a [StateFlow] within the given [scope].
     * The [StateFlow] is typically started when there are subscribers and shares the most recent value.
     * It will be initialized with the current preference value (or [defaultValue] if not set or on error).
     * @param scope The [CoroutineScope] in which to launch the [StateFlow].
     * @param started The [SharingStarted] strategy that controls when the upstream flow is active.
     * @return A [StateFlow] representing the preference's value.
     */
    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<T> =
        asFlow().stateIn(scope, started, defaultValue)

    /**
     * Synchronously retrieves the current value of the preference.
     * This operation may block the calling thread while accessing DataStore.
     * If the key is not found or an error occurs, this function returns the [defaultValue].
     * Use with caution due to potential blocking.
     */
    override fun getBlocking(): T = runBlocking {
        get()
    }

    /**
     * Synchronously sets the value of the preference.
     * This operation may block the calling thread while accessing DataStore.
     * Use with caution due to potential blocking.
     * @param value The new value to store for this preference.
     */
    override fun setBlocking(value: T) {
        runBlocking {
            set(value)
        }
    }

    override fun readFrom(preferences: Preferences): T =
        preferences[this.preferences] ?: defaultValue

    override fun writeInto(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[this.preferences] = value
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(this.preferences)
    }
}
