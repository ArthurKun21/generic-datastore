package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.batch.PreferencesAccessor
import io.github.arthurkun.generic.datastore.preferences.utils.dataOrEmpty
import io.github.arthurkun.generic.datastore.preferences.utils.deserializeOrNull
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
 * Base implementation for nullable preferences stored as a single entry of type [S].
 *
 * Missing keys and decode failures both read back as `null`. Writing `null` removes the key from
 * DataStore.
 *
 * @param T The non-null exposed value type.
 * @param S The stored value type (`String`, `Int`, `Long`, `Float`, `Double`, …).
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param preferences The typed [Preferences.Key] used to access the stored [S] value.
 * @param serializer Converts [T] to its stored [S] representation.
 * @param deserializer Converts a stored [S] back to [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 */
internal sealed class NullableCustomGenericPreferenceItem<T : Any, S>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    private val preferences: Preferences.Key<S>,
    private val serializer: (T) -> S,
    private val deserializer: (S) -> T,
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
                    ds[preferences] = serializer(value)
                }
            }
        }
    }

    override suspend fun update(transform: (T?) -> T?) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                val current = ds[preferences]?.let { safeDeserialize(it) }
                val newValue = transform(current)
                if (newValue == null) {
                    ds.remove(preferences)
                } else {
                    ds[preferences] = serializer(newValue)
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

    override suspend fun resetToDefault(): Unit = delete()

    override fun asFlow(): Flow<T?> {
        return datastore.dataOrEmpty.map { prefs ->
            prefs[preferences]?.let { safeDeserialize(it) }
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

    private fun safeDeserialize(value: S): T? = deserializeOrNull(value, deserializer)

    override fun readFrom(preferences: Preferences): T? =
        preferences[this.preferences]?.let { safeDeserialize(it) }

    override fun writeInto(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(this.preferences)
        } else {
            mutablePreferences[this.preferences] = serializer(value)
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(this.preferences)
    }
}
