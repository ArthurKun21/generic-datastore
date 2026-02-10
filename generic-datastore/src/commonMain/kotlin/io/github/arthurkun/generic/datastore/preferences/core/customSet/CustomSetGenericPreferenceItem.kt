package io.github.arthurkun.generic.datastore.preferences.core.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base sealed class for [Preference] implementations that store values as strings
 * via [stringSetPreferencesKey].
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
internal sealed class CustomSetGenericPreferenceItem<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: Set<T>,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<Set<T>> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    private val stringSetPrefKey = stringSetPreferencesKey(key)

    override fun key(): String = key

    override suspend fun get(): Set<T> {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    override suspend fun set(value: Set<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs[stringSetPrefKey] = value.map { serializer(it) }.toSet()
            }
        }
    }

    override suspend fun update(transform: (Set<T>) -> Set<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                val current = prefs[stringSetPrefKey]?.let { safeDeserializeSet(it) }
                    ?: defaultValue
                prefs[stringSetPrefKey] = transform(current).map { serializer(it) }.toSet()
            }
        }
    }

    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs.remove(stringSetPrefKey)
            }
        }
    }

    override suspend fun resetToDefault() = set(defaultValue)

    override fun asFlow(): Flow<Set<T>> {
        return datastore.dataOrEmpty.map { prefs ->
            prefs[stringSetPrefKey]?.let { safeDeserializeSet(it) }
                ?: this.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<Set<T>> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): Set<T> = runBlocking { get() }

    override fun setBlocking(value: Set<T>) = runBlocking { set(value) }

    private fun safeDeserializeSet(values: Set<String>): Set<T> {
        return values.mapNotNull { value ->
            try {
                deserializer(value)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }.toSet()
    }
}
