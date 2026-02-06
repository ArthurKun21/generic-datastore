package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

/**
 * A [Preference] for storing custom [Object] values.
 * This class handles the serialization of the object to a String for storage
 * and deserialization from String back to the object on retrieval.
 *
 * If deserialization fails (e.g., due to corrupted data), the [defaultValue] is returned.
 *
 * @param T The type of the custom object.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set or an error occurs during deserialization.
 * @param serializer A function to serialize the object of type [T] to its String representation for storage.
 * @param deserializer A function to deserialize the String representation back to an object of type [T].
 */
internal class ObjectPrimitive<T>(
    private val datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    val serializer: (T) -> String,
    val deserializer: (String) -> T,
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
                        ?: this@ObjectPrimitive.defaultValue
                }
                .first()
        }
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                prefs[stringPrefKey] = serializer(value)
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

    override fun asFlow(): Flow<T> {
        return datastore.data.map { prefs ->
            prefs[stringPrefKey]?.let { safeDeserialize(it) } ?: this@ObjectPrimitive.defaultValue
        }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    override fun getBlocking(): T = runBlocking { get() }

    override fun setBlocking(value: T) = runBlocking { set(value) }

    private fun safeDeserialize(value: String): T {
        return try {
            deserializer(value)
        } catch (_: Exception) {
            defaultValue
        }
    }
}
