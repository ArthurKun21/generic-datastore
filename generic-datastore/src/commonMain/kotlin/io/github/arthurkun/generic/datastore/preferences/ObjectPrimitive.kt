package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [GenericPreferenceItem] for storing custom [Object] values.
 * This class handles the serialization of the object to a String for storage
 * and deserialization from String back to the object on retrieval.
 *
 * @param T The type of the custom object.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set or an error occurs during deserialization.
 * @param serializer A function to serialize the object of type [T] to its String representation for storage.
 * @param deserializer A function to deserialize the String representation back to an object of type [T].
 */
@Suppress("UNCHECKED_CAST")
internal class ObjectPrimitive<T>(
    datastore: DataStore<Preferences>,
    private val key: String,
    override val defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GenericPreferenceItem<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key) as Preferences.Key<T>,
) {
    private val stringPrefKey = stringPreferencesKey(key)

    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { prefs ->
                    prefs[stringPrefKey]?.let { deserializer(it) }
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

    override suspend fun update(transform: (T) -> T) {
        withContext(ioDispatcher) {
            datastore.edit { prefs ->
                val current = prefs[stringPrefKey]?.let { safeDeserialize(it) }
                    ?: this@ObjectPrimitive.defaultValue
                prefs[stringPrefKey] = serializer(transform(current))
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
            prefs[stringPrefKey]?.let { deserializer(it) } ?: this@ObjectPrimitive.defaultValue
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
