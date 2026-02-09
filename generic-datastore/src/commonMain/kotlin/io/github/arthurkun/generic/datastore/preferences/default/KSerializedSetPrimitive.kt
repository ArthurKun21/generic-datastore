package io.github.arthurkun.generic.datastore.preferences.default

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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [Preference] for storing a [Set] of custom objects using Kotlin Serialization.
 * Each element is serialized to a JSON String and stored using [stringSetPreferencesKey].
 * On retrieval, each String element is deserialized back via the provided [KSerializer].
 *
 * If deserialization of an individual element fails, that element is skipped.
 *
 * @param T The type of each element in the set. Must be serializable using kotlinx.serialization.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set.
 * @param serializer The [KSerializer] for the type [T].
 * @param json The [Json] instance to use for serialization/deserialization.
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations. Defaults to [Dispatchers.IO].
 */
internal class KSerializedSetPrimitive<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: Set<T>,
    private val serializer: KSerializer<T>,
    private val json: Json,
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
                prefs[stringSetPrefKey] = value.map { json.encodeToString(serializer, it) }.toSet()
            }
        }
    }

    override suspend fun update(transform: (Set<T>) -> Set<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                val current = prefs[stringSetPrefKey]?.let { safeDeserializeSet(it) }
                    ?: this@KSerializedSetPrimitive.defaultValue
                prefs[stringSetPrefKey] = transform(current).map { json.encodeToString(serializer, it) }.toSet()
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
                ?: this@KSerializedSetPrimitive.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope, started: SharingStarted): StateFlow<Set<T>> =
        asFlow().stateIn(scope, started, defaultValue)

    override fun getBlocking(): Set<T> = runBlocking { get() }

    override fun setBlocking(value: Set<T>) = runBlocking { set(value) }

    private fun safeDeserializeSet(values: Set<String>): Set<T> {
        return values.mapNotNull { value ->
            try {
                json.decodeFromString(serializer, value)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }.toSet()
    }
}
