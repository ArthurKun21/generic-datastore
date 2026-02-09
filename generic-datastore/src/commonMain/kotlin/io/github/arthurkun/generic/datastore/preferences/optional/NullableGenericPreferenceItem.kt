package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.arthurkun.generic.datastore.core.Preference
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
 * Represents a nullable generic preference that can be stored in and retrieved from a DataStore.
 *
 * This sealed class provides a type-safe way to handle nullable preference types.
 * When a key is not set in DataStore, `null` is returned instead of a default value.
 * Setting a `null` value removes the key from DataStore.
 *
 * @param T The non-null data type of the preference value (e.g., String, Int, Boolean).
 * @property datastore The [DataStore] instance used for storing and retrieving preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param preferences The [Preferences.Key] specific to the type [T], used to access the preference in DataStore.
 */
internal sealed class NullableGenericPreferenceItem<T : Any>(
    internal val datastore: DataStore<Preferences>,
    private val key: String,
    private val preferences: Preferences.Key<T>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<T?> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    override val defaultValue: T? = null

    override fun key(): String = key

    override suspend fun get(): T? {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    override suspend fun set(value: T?) {
        withContext(ioDispatcher) {
            if (value == null) {
                datastore.edit { ds ->
                    ds.remove(preferences)
                }
            } else {
                datastore.edit { ds ->
                    ds[preferences] = value
                }
            }
        }
    }

    override suspend fun update(transform: (T?) -> T?) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                val current = ds[preferences]
                val newValue = transform(current)
                if (newValue == null) {
                    ds.remove(preferences)
                } else {
                    ds[preferences] = newValue
                }
            }
        }
    }

    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds.remove(preferences)
            }
        }
    }

    override suspend fun resetToDefault() = delete()

    override fun asFlow(): Flow<T?> {
        return datastore
            .dataOrEmpty
            .map { preferences ->
                preferences[this.preferences]
            }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T?> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, null)

    override fun getBlocking(): T? = runBlocking {
        get()
    }

    override fun setBlocking(value: T?) {
        runBlocking {
            set(value)
        }
    }
}
