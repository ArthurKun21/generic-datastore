package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.core.BasePreference
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
 * Base sealed class for [BasePreference] implementations that store values as strings
 * via [stringPreferencesKey].
 *
 * Subclasses provide [serializer] and [deserializer] functions to convert between [T]
 * and its [String] representation. If deserialization fails (e.g., due to corrupted data),
 * the [defaultValue] is returned instead.
 *
 * @param T The type of the preference value.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param defaultValue The default value returned when the preference is not set or
 *   deserialization fails.
 * @param serializer A function to convert a value of type [T] to its [String] representation.
 * @param deserializer A function to convert a [String] representation back to a value of type [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 */
internal sealed class CustomGenericPreferenceItem<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BasePreference<T> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    private val stringPrefKey = stringPreferencesKey(key)

    override fun key(): String = key

    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                ds[stringPrefKey] = serializer(value)
            }
        }
    }

    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.edit { ds ->
                val current = ds[stringPrefKey]?.let {
                    safeDeserialize(it)
                } ?: defaultValue
                ds[stringPrefKey] = serializer(transform(current))
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

    override suspend fun resetToDefault() = set(defaultValue)

    override fun asFlow(): Flow<T> {
        return datastore.dataOrEmpty.map { prefs ->
            prefs[stringPrefKey]?.let { safeDeserialize(it) }
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

    private fun safeDeserialize(value: String): T {
        return try {
            deserializer(value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            defaultValue
        }
    }
}
