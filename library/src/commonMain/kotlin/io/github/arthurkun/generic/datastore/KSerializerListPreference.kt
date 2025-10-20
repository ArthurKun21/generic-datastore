package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * A [GenericPreference] for storing lists/collections of custom objects using kotlinx.serialization.
 * This class uses [KSerializer] to serialize each element in the collection individually,
 * storing them as a Set<String> in DataStore for efficient storage and retrieval.
 *
 * Each element in the collection is serialized to JSON separately, allowing for efficient
 * updates of individual items without re-serializing the entire collection.
 *
 * @param T The type of elements in the collection, must be serializable with kotlinx.serialization.
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default collection to be returned if the preference is not set or an error occurs.
 * @param serializer The [KSerializer] for type [T] used for serializing/deserializing individual elements.
 * @param json Optional [Json] instance for customizing serialization behavior. Defaults to Json.Default.
 */
@Suppress("UNCHECKED_CAST")
class KSerializerListPreference<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: List<T>,
    val serializer: KSerializer<T>,
    val json: Json = Json.Default,
) : GenericPreference<List<T>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringSetPreferencesKey(key) as Preferences.Key<List<T>>,
) {
    private val stringSetPrefKey = stringSetPreferencesKey(key)

    private val ioDispatcher = Dispatchers.IO

    override suspend fun get(): List<T> {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { prefs ->
                    prefs[stringSetPrefKey]?.let { storedSet ->
                        try {
                            storedSet.mapNotNull { serializedItem ->
                                try {
                                    json.decodeFromString(serializer, serializedItem)
                                } catch (e: SerializationException) {
                                    ConsoleLogger.error(
                                        "Failed to deserialize list item for key '$key' using KSerializer",
                                        e,
                                    )
                                    null // Skip invalid items
                                } catch (e: Exception) {
                                    ConsoleLogger.error(
                                        "Unexpected error deserializing list item for key '$key'",
                                        e,
                                    )
                                    null
                                }
                            }
                        } catch (e: Exception) {
                            ConsoleLogger.error(
                                "Failed to process stored set for key '$key'",
                                e,
                            )
                            this@KSerializerListPreference.defaultValue
                        }
                    } ?: this@KSerializerListPreference.defaultValue
                }
                .first()
        }
    }

    override suspend fun set(value: List<T>) {
        withContext(ioDispatcher) {
            try {
                val serializedSet = value.mapNotNull { item ->
                    try {
                        json.encodeToString(serializer, item)
                    } catch (e: SerializationException) {
                        ConsoleLogger.error(
                            "Failed to serialize list item for key '$key' using KSerializer",
                            e,
                        )
                        null // Skip items that fail to serialize
                    } catch (e: Exception) {
                        ConsoleLogger.error(
                            "Unexpected error serializing list item for key '$key'",
                            e,
                        )
                        null
                    }
                }.toSet()

                datastore.edit { prefs ->
                    prefs[stringSetPrefKey] = serializedSet
                }
            } catch (e: Exception) {
                ConsoleLogger.error(
                    "Failed to set list value for key '$key'",
                    e,
                )
                // Don't update the preference if serialization fails
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

    override fun asFlow(): Flow<List<T>> {
        return datastore.data.map { prefs ->
            prefs[stringSetPrefKey]?.let { storedSet ->
                try {
                    storedSet.mapNotNull { serializedItem ->
                        try {
                            json.decodeFromString(serializer, serializedItem)
                        } catch (e: SerializationException) {
                            ConsoleLogger.error(
                                "Failed to deserialize list item for key '$key' in flow using KSerializer",
                                e,
                            )
                            null // Skip invalid items
                        } catch (e: Exception) {
                            ConsoleLogger.error(
                                "Unexpected error deserializing list item for key '$key' in flow",
                                e,
                            )
                            null
                        }
                    }
                } catch (e: Exception) {
                    ConsoleLogger.error(
                        "Failed to process stored set for key '$key' in flow",
                        e,
                    )
                    this@KSerializerListPreference.defaultValue
                }
            } ?: this@KSerializerListPreference.defaultValue
        }
    }
}
