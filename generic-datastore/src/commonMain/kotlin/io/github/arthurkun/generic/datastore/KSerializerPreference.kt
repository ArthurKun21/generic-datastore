package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * A [GenericPreference] for storing custom objects using kotlinx.serialization.
 * The object is serialized to JSON string format for storage.
 *
 * @param T The type of the custom object (must be serializable)
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default value to be returned if the preference is not set or an error occurs during deserialization.
 * @param serializer The KSerializer for type T
 * @param json The Json instance to use for serialization (defaults to a standard configuration)
 */
@Suppress("Unchecked_cast")
class KSerializerPreference<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: T,
    private val serializer: KSerializer<T>,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GenericPreference<T>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key) as Preferences.Key<T>,
) {
    private val stringPrefKey = stringPreferencesKey(key)
    private val ioDispatcher = Dispatchers.IO
    private val mutex = Mutex()

    override suspend fun get(): T {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { prefs ->
                    prefs[stringPrefKey]?.let {
                        try {
                            json.decodeFromString(serializer, it)
                        } catch (e: SerializationException) {
                            println("$TAG: Error deserializing preference $key: ${e.message}")
                            this@KSerializerPreference.defaultValue
                        }
                    } ?: this@KSerializerPreference.defaultValue
                }
                .first()
        }
    }

    override suspend fun set(value: T) {
        mutex.withLock {
            withContext(ioDispatcher) {
                try {
                    datastore.edit { prefs ->
                        prefs[stringPrefKey] = json.encodeToString(serializer, value)
                    }
                } catch (e: SerializationException) {
                    println("$TAG: Error serializing preference $key: ${e.message}")
                }
            }
        }
    }

    override suspend fun getAndSet(value: T): T {
        return mutex.withLock {
            withContext(ioDispatcher) {
                val currentValue = datastore
                    .data
                    .map { prefs ->
                        prefs[stringPrefKey]?.let {
                            try {
                                json.decodeFromString(serializer, it)
                            } catch (e: SerializationException) {
                                println("$TAG: Error deserializing preference $key: ${e.message}")
                                this@KSerializerPreference.defaultValue
                            }
                        } ?: this@KSerializerPreference.defaultValue
                    }
                    .first()
                try {
                    datastore.edit { prefs ->
                        prefs[stringPrefKey] = json.encodeToString(serializer, value)
                    }
                } catch (e: SerializationException) {
                    println("$TAG: Error serializing preference $key: ${e.message}")
                }
                currentValue
            }
        }
    }

    override suspend fun delete() {
        mutex.withLock {
            withContext(ioDispatcher) {
                datastore.edit { prefs ->
                    prefs.remove(stringPrefKey)
                }
            }
        }
    }

    override fun asFlow(): Flow<T> {
        return datastore.data.map { prefs ->
            prefs[stringPrefKey]?.let {
                try {
                    json.decodeFromString(serializer, it)
                } catch (e: SerializationException) {
                    println("$TAG: Error deserializing preference $key in flow: ${e.message}")
                    this@KSerializerPreference.defaultValue
                }
            } ?: this@KSerializerPreference.defaultValue
        }
    }
}

/**
 * A [GenericPreference] for storing lists/collections of objects using kotlinx.serialization.
 * The list is serialized and stored as a Set<String> where each element is a JSON string.
 *
 * This is more efficient for large collections as DataStore handles string sets natively.
 *
 * @param T The type of the objects in the list (must be serializable)
 * @param datastore The [DataStore<Preferences>] instance used for storing preferences.
 * @param key The unique String key used to identify this preference within the DataStore.
 * @param defaultValue The default list to be returned if the preference is not set or an error occurs.
 * @param serializer The KSerializer for type T (for individual elements)
 * @param json The Json instance to use for serialization (defaults to a standard configuration)
 */
@Suppress("Unchecked_cast")
class KSerializerListPreference<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: List<T>,
    private val serializer: KSerializer<T>,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GenericPreference<List<T>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringSetPreferencesKey(key) as Preferences.Key<List<T>>,
) {
    private val stringSetPrefKey = stringSetPreferencesKey(key)
    private val ioDispatcher = Dispatchers.IO
    private val mutex = Mutex()

    override suspend fun get(): List<T> {
        return withContext(ioDispatcher) {
            datastore
                .data
                .map { prefs ->
                    prefs[stringSetPrefKey]?.mapNotNull {
                        try {
                            json.decodeFromString(serializer, it)
                        } catch (e: SerializationException) {
                            println("$TAG: Error deserializing list element in $key: ${e.message}")
                            null
                        }
                    } ?: this@KSerializerListPreference.defaultValue
                }
                .first()
        }
    }

    override suspend fun set(value: List<T>) {
        mutex.withLock {
            withContext(ioDispatcher) {
                try {
                    val stringSet = value.mapNotNull {
                        try {
                            json.encodeToString(serializer, it)
                        } catch (e: SerializationException) {
                            println("$TAG: Error serializing list element in $key: ${e.message}")
                            null
                        }
                    }.toSet()
                    datastore.edit { prefs ->
                        prefs[stringSetPrefKey] = stringSet
                    }
                } catch (e: Exception) {
                    println("$TAG: Error setting list preference $key: ${e.message}")
                }
            }
        }
    }

    override suspend fun getAndSet(value: List<T>): List<T> {
        return mutex.withLock {
            withContext(ioDispatcher) {
                val currentValue = datastore
                    .data
                    .map { prefs ->
                        prefs[stringSetPrefKey]?.mapNotNull {
                            try {
                                json.decodeFromString(serializer, it)
                            } catch (e: SerializationException) {
                                println("$TAG: Error deserializing list element in $key: ${e.message}")
                                null
                            }
                        } ?: this@KSerializerListPreference.defaultValue
                    }
                    .first()
                try {
                    val stringSet = value.mapNotNull {
                        try {
                            json.encodeToString(serializer, it)
                        } catch (e: SerializationException) {
                            println("$TAG: Error serializing list element in $key: ${e.message}")
                            null
                        }
                    }.toSet()
                    datastore.edit { prefs ->
                        prefs[stringSetPrefKey] = stringSet
                    }
                } catch (e: Exception) {
                    println("$TAG: Error in getAndSet for list preference $key: ${e.message}")
                }
                currentValue
            }
        }
    }

    override suspend fun delete() {
        mutex.withLock {
            withContext(ioDispatcher) {
                datastore.edit { prefs ->
                    prefs.remove(stringSetPrefKey)
                }
            }
        }
    }

    override fun asFlow(): Flow<List<T>> {
        return datastore.data.map { prefs ->
            prefs[stringSetPrefKey]?.mapNotNull {
                try {
                    json.decodeFromString(serializer, it)
                } catch (e: SerializationException) {
                    println("$TAG: Error deserializing list element in flow for $key: ${e.message}")
                    null
                }
            } ?: this@KSerializerListPreference.defaultValue
        }
    }
}

/**
 * Extension function to create a preference with kotlinx.serialization support.
 *
 * @param T The type of the custom object (must be serializable)
 * @param key The preference key
 * @param defaultValue The default value
 * @param serializer The KSerializer for type T
 * @param json Optional Json configuration
 * @return A [Prefs] instance for the serializable object
 */
fun <T> DatastoreRepository.kSerialized(
    key: String,
    defaultValue: T,
    serializer: KSerializer<T>,
    json: Json = Json { ignoreUnknownKeys = true },
): Prefs<T> = PrefsImpl(
    KSerializerPreference(
        datastore = (this as DatastoreManager).datastore,
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        json = json,
    ),
)

/**
 * Extension function to create a list preference with kotlinx.serialization support.
 *
 * @param T The type of objects in the list (must be serializable)
 * @param key The preference key
 * @param defaultValue The default list value
 * @param serializer The KSerializer for individual elements of type T
 * @param json Optional Json configuration
 * @return A [Prefs] instance for the list of serializable objects
 */
fun <T> DatastoreRepository.kSerializedList(
    key: String,
    defaultValue: List<T> = emptyList(),
    serializer: KSerializer<T>,
    json: Json = Json { ignoreUnknownKeys = true },
): Prefs<List<T>> = PrefsImpl(
    KSerializerListPreference(
        datastore = (this as DatastoreManager).datastore,
        key = key,
        defaultValue = defaultValue,
        serializer = serializer,
        json = json,
    ),
)
