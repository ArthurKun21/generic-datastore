package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.core.Preference
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [Preference] for storing custom objects using Kotlin Serialization.
 * This class handles the serialization of the object to a JSON String for storage
 * and deserialization from JSON String back to the object on retrieval.
 *
 * If deserialization fails (e.g., due to corrupted data), the [defaultValue] is returned.
 *
 * @param T The type of the custom object. Must be serializable using kotlinx.serialization.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set or an error occurs during deserialization.
 * @param serializer The [KSerializer] for the type [T].
 * @param json The [Json] instance to use for serialization/deserialization. Defaults to a lenient, ignoreUnknownKeys instance.
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations. Defaults to [Dispatchers.IO].
 */
internal class KSerializedPrimitive<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val json: Json = defaultJson,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<T> {
    private val stringPrefKey = stringPreferencesKey(key)

    override fun key(): String = key

    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { prefs ->
                    prefs[stringPrefKey]?.let { safeDeserialize(it) }
                        ?: this@KSerializedPrimitive.defaultValue
                }
                .first()
        }
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs[stringPrefKey] = json.encodeToString(serializer, value)
            }
        }
    }

    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                val current = prefs[stringPrefKey]?.let { safeDeserialize(it) }
                    ?: this@KSerializedPrimitive.defaultValue
                prefs[stringPrefKey] = json.encodeToString(serializer, transform(current))
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

    override fun asFlow(): Flow<T> {
        return datastore.data.map { prefs ->
            prefs[stringPrefKey]?.let { safeDeserialize(it) } ?: this@KSerializedPrimitive.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    override fun getBlocking(): T = runBlocking { get() }

    override fun setBlocking(value: T) = runBlocking { set(value) }

    private fun safeDeserialize(value: String): T {
        return try {
            json.decodeFromString(serializer, value)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            defaultValue
        }
    }
}
