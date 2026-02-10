package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
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
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base sealed class for nullable [BasePreference] implementations that store values as strings
 * via [stringPreferencesKey].
 *
 * Subclasses provide [serializer] and [deserializer] functions to convert between [T]
 * and its [String] representation. When a key is not set in DataStore, `null` is returned.
 * Setting a `null` value removes the key from DataStore. If deserialization fails
 * (e.g., due to corrupted data), `null` is returned.
 *
 * @param T The non-null type of the preference value.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param serializer A function to convert a value of type [T] to its [String] representation.
 * @param deserializer A function to convert a [String] representation back to a value of type [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 */
internal sealed class NullableCustomGenericPreferenceItem<T : Any>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BasePreference<T?>, PreferencesAccessor<T?> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    private val stringPrefKey = stringPreferencesKey(key)

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
                    ds.remove(stringPrefKey)
                }
            } else {
                datastore.edit { ds ->
                    ds[stringPrefKey] = serializer(value)
                }
            }
        }
    }

    override suspend fun update(transform: (T?) -> T?) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                val current = ds[stringPrefKey]?.let { safeDeserialize(it) }
                val newValue = transform(current)
                if (newValue == null) {
                    ds.remove(stringPrefKey)
                } else {
                    ds[stringPrefKey] = serializer(newValue)
                }
            }
        }
    }

    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds.remove(stringPrefKey)
            }
        }
    }

    override suspend fun resetToDefault() = delete()

    override fun asFlow(): Flow<T?> {
        return datastore.dataOrEmpty.map { prefs ->
            prefs[stringPrefKey]?.let { safeDeserialize(it) }
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

    private fun safeDeserialize(value: String): T? {
        return try {
            deserializer(value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override fun readFrom(preferences: Preferences): T? =
        preferences[stringPrefKey]?.let { safeDeserialize(it) }

    override fun writeInto(mutablePreferences: MutablePreferences, value: T?) {
        if (value == null) {
            mutablePreferences.remove(stringPrefKey)
        } else {
            mutablePreferences[stringPrefKey] = serializer(value)
        }
    }

    override fun removeFrom(mutablePreferences: MutablePreferences) {
        mutablePreferences.remove(stringPrefKey)
    }
}
