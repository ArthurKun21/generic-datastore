package io.github.arthurkun.generic.datastore.preferences.default

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [Preference] for storing a [List] of custom objects using Kotlin Serialization.
 * The list is serialized to a JSON array string and stored using [stringPreferencesKey].
 * On retrieval, the JSON array string is deserialized back via the provided [KSerializer].
 *
 * If deserialization fails (e.g., due to corrupted data), the [defaultValue] is returned.
 *
 * @param T The type of each element in the list. Must be serializable using kotlinx.serialization.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set.
 * @param serializer The [KSerializer] for the type [T].
 * @param json The [Json] instance to use for serialization/deserialization.
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations. Defaults to [Dispatchers.IO].
 */
internal class KSerializedListPrimitive<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: List<T>,
    private val serializer: KSerializer<T>,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<List<T>> {

    init {
        require(key.isNotBlank()) {
            "Preference key cannot be blank."
        }
    }

    private val stringPrefKey = stringPreferencesKey(key)
    private val listSerializer = ListSerializer(serializer)

    override fun key(): String = key

    override suspend fun get(): List<T> {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    override suspend fun set(value: List<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs[stringPrefKey] = json.encodeToString(listSerializer, value)
            }
        }
    }

    override suspend fun update(transform: (List<T>) -> List<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                val current = prefs[stringPrefKey]?.let { safeDeserialize(it) }
                    ?: this@KSerializedListPrimitive.defaultValue
                prefs[stringPrefKey] = json.encodeToString(listSerializer, transform(current))
            }
        }
    }

    override suspend fun delete() {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs.remove(stringPrefKey)
            }
        }
    }

    override suspend fun resetToDefault() = set(defaultValue)

    override fun asFlow(): Flow<List<T>> {
        return datastore.dataOrEmpty.map { prefs ->
            prefs[stringPrefKey]?.let { safeDeserialize(it) }
                ?: this@KSerializedListPrimitive.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<List<T>> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): List<T> = runBlocking { get() }

    override fun setBlocking(value: List<T>) = runBlocking { set(value) }

    private fun safeDeserialize(value: String): List<T> {
        return try {
            json.decodeFromString(listSerializer, value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            defaultValue
        }
    }
}
