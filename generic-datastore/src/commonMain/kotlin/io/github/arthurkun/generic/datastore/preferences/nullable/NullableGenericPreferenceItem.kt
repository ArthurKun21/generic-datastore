package io.github.arthurkun.generic.datastore.preferences.nullable

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.arthurkun.generic.datastore.core.Preference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal sealed class NullableGenericPreferenceItem<T : Any>(
    internal val datastore: DataStore<Preferences>,
    private val key: String,
    private val preferences: Preferences.Key<T>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<T?> {

    override val defaultValue: T? = null

    override fun key(): String = key

    override suspend fun get(): T? {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { preferences ->
                    preferences[this@NullableGenericPreferenceItem.preferences]
                }
                .first()
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
            .data
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
