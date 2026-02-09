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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [Preference] for storing a [List] of custom objects using per-element serialization.
 * The list is serialized to a JSON array string and stored using [stringPreferencesKey].
 * On retrieval, each element in the JSON array is deserialized back via [deserializer].
 *
 * If deserialization of an individual element fails, that element is skipped.
 *
 * @param T The type of each element in the list.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set.
 * @param serializer A function to serialize an element of type [T] to its String representation.
 * @param deserializer A function to deserialize a String back to an element of type [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations. Defaults to [Dispatchers.IO].
 */
internal class SerializedListPrimitive<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: List<T>,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<List<T>> {
    private val stringPrefKey = stringPreferencesKey(key)

    override fun key(): String = key

    override suspend fun get(): List<T> {
        return withContext(ioDispatcher) {
            asFlow().first()
        }
    }

    override suspend fun set(value: List<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs[stringPrefKey] = serializeList(value)
            }
        }
    }

    override suspend fun update(transform: (List<T>) -> List<T>) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                val current = prefs[stringPrefKey]?.let { safeDeserializeList(it) }
                    ?: this@SerializedListPrimitive.defaultValue
                prefs[stringPrefKey] = serializeList(transform(current))
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
            prefs[stringPrefKey]?.let { safeDeserializeList(it) }
                ?: this@SerializedListPrimitive.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<List<T>> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    override fun getBlocking(): List<T> = runBlocking { get() }

    override fun setBlocking(value: List<T>) = runBlocking { set(value) }

    private fun serializeList(value: List<T>): String {
        val jsonArray = JsonArray(value.map { JsonPrimitive(serializer(it)) })
        return jsonArray.toString()
    }

    private fun safeDeserializeList(value: String): List<T> {
        return try {
            Json.parseToJsonElement(value).jsonArray.mapNotNull { element ->
                try {
                    deserializer(element.jsonPrimitive.content)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            defaultValue
        }
    }
}
