package io.github.arthurkun.generic.datastore.preferences.optional

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
 * Base implementation for nullable Preferences DataStore entries.
 *
 * Missing keys read back as `null`, and writing `null` removes the key from DataStore.
 * Subclasses bind a concrete [Preferences.Key] for each primitive nullable type.
 *
 * @param T The non-null stored value type.
 * @property datastore The [DataStore] instance used for storage.
 * @param key The unique preference key name.
 * @param preferences The typed [Preferences.Key] used to access this value in DataStore.
 */
internal sealed class NullableGenericPreferenceItem<T : Any>(
    internal val datastore: DataStore<Preferences>,
    private val key: String,
    private val preferences: Preferences.Key<T>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BasePreference<T?>, PreferencesAccessor<T?> {

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

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<T?> =
        asFlow().stateIn(scope, started, null)

    override fun getBlocking(): T? = runBlocking {
        get()
    }

    override fun setBlocking(value: T?) {
        runBlocking {
            set(value)
        }
    }

    override fun readFrom(preferences: Preferences): T? =
        preferences[this.preferences]

    override fun writeInto(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(this.preferences)
        } else {
            mutablePreferences[this.preferences] = value
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(this.preferences)
    }
}
