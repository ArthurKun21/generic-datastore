package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * A [GenericPreference] for storing custom objects using kotlinx.serialization.
 * This class uses [KSerializer] to handle serialization and deserialization,
 * providing type-safe serialization with compile-time validation.
 *
 * The object is serialized to JSON and stored as a String in DataStore.
 *
 * @param T The type of the custom object, must be serializable with kotlinx.serialization.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set or an error occurs during deserialization.
 * @param serializer The [KSerializer] for type [T] used for serialization and deserialization.
 * @param json Optional [Json] instance for customizing serialization behavior. Defaults to Json.Default.
 */
@Suppress("UNCHECKED_CAST")
class KSerializerPreference<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    val serializer: KSerializer<T>,
    val json: Json = Json.Default,
) : GenericPreference<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key) as Preferences.Key<T>,
) {
    private val stringPrefKey = stringPreferencesKey(key)

    private val ioDispatcher = Dispatchers.Default

    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { prefs ->
                    prefs[stringPrefKey]?.let { storedValue ->
                        try {
                            json.decodeFromString(serializer, storedValue)
                        } catch (e: SerializationException) {
                            ConsoleLogger.error(
                                "Failed to deserialize value for key '$key' using KSerializer",
                                e,
                            )
                            this@KSerializerPreference.defaultValue
                        } catch (e: Exception) {
                            ConsoleLogger.error(
                                "Unexpected error deserializing value for key '$key'",
                                e,
                            )
                            this@KSerializerPreference.defaultValue
                        }
                    } ?: this@KSerializerPreference.defaultValue
                }
                .first()
        }
    }

    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            try {
                val serializedValue = json.encodeToString(serializer, value)
                datastore.edit { prefs ->
                    prefs[stringPrefKey] = serializedValue
                }
            } catch (e: SerializationException) {
                ConsoleLogger.error(
                    "Failed to serialize value for key '$key' using KSerializer",
                    e,
                )
                // Don't update the preference if serialization fails
            } catch (e: Exception) {
                ConsoleLogger.error(
                    "Unexpected error serializing value for key '$key'",
                    e,
                )
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
            prefs[stringPrefKey]?.let { storedValue ->
                try {
                    json.decodeFromString(serializer, storedValue)
                } catch (e: SerializationException) {
                    ConsoleLogger.error(
                        "Failed to deserialize value for key '$key' in flow using KSerializer",
                        e,
                    )
                    this@KSerializerPreference.defaultValue
                } catch (e: Exception) {
                    ConsoleLogger.error(
                        "Unexpected error deserializing value for key '$key' in flow",
                        e,
                    )
                    this@KSerializerPreference.defaultValue
                }
            } ?: this@KSerializerPreference.defaultValue
        }
    }
}
