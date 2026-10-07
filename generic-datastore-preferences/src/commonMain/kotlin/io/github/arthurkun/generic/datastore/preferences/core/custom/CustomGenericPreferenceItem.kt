package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.batch.PreferencesAccessor
import io.github.arthurkun.generic.datastore.preferences.utils.dataOrEmpty
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeOrDefault
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
 * Base implementation for non-null preferences stored as a single entry of type [S].
 *
 * Subclasses bind a concrete [Preferences.Key] for the storage type [S] and provide [serializer]
 * and [deserializer] functions that convert between [T] and its stored form. Missing keys and
 * decode failures fall back to [defaultValue].
 *
 * @param T The exposed value type.
 * @param S The stored value type (`String`, `Int`, `Long`, `Float`, `Double`, …).
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param defaultValue The value returned when the key is missing or deserialization fails.
 * @param preferences The typed [Preferences.Key] used to access the stored [S] value.
 * @param serializer Converts [T] to its stored [S] representation.
 * @param deserializer Converts a stored [S] back to [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 */
internal sealed class CustomGenericPreferenceItem<T, S>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val preferences: Preferences.Key<S>,
    private val serializer: (T) -> S,
    private val deserializer: (S) -> T,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BasePreference<T>, PreferencesAccessor<T> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    override fun key(): String = key

    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds[preferences] = serializer(value)
            }
        }
    }

    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                val current = ds[preferences]?.let {
                    safeDeserialize(it)
                } ?: defaultValue
                ds[preferences] = serializer(transform(current))
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

    override suspend fun resetToDefault(): Unit = set(defaultValue)

    override fun asFlow(): Flow<T> {
        return datastore.dataOrEmpty.map { prefs ->
            prefs[preferences]?.let { safeDeserialize(it) }
                ?: this.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<T> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): T = runBlocking {
        get()
    }

    override fun setBlocking(value: T) {
        runBlocking {
            set(value)
        }
    }

    private fun safeDeserialize(value: S): T = deserializeOrDefault(value, defaultValue, deserializer)

    override fun readFrom(preferences: Preferences): T =
        preferences[this.preferences]?.let { safeDeserialize(it) } ?: defaultValue

    override fun writeInto(mutablePreferences: MutablePreferences, value: T) {
        mutablePreferences[this.preferences] = serializer(value)
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(this.preferences)
    }
}
