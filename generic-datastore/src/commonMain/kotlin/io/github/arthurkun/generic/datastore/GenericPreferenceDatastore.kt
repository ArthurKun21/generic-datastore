package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.GenericPreference.BooleanPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.FloatPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.IntPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.LongPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.StringPrimitive
import io.github.arthurkun.generic.datastore.GenericPreference.StringSetPrimitive
import io.github.arthurkun.generic.datastore.Preference.Companion.isAppState
import io.github.arthurkun.generic.datastore.Preference.Companion.isPrivate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

/**
 * Configuration for the DatastoreManager in-memory cache.
 *
 * This cache uses a high-performance, coroutine-first design inspired by Caffeine,
 * with segmented locking for better concurrency and frequency-based eviction.
 *
 * @property enabled Whether caching is enabled
 * @property maxSize Maximum number of entries in the cache
 * @property segmentCount Number of segments for concurrent access (default: 4).
 *   Higher values improve concurrency but use more memory.
 * @property expiryMillis Time in milliseconds after which entries expire (0 = no expiry)
 * @property recordStats Whether to record cache statistics for monitoring (default: false)
 */
data class CacheConfig(
    val enabled: Boolean = true,
    val maxSize: Int = 100,
    val segmentCount: Int = 4,
    val expiryMillis: Long = 0,
    val recordStats: Boolean = false,
)

/**
 * A DataStore implementation that provides methods for creating and managing various types of preferences.
 *
 * This class wraps a [DataStore<Preferences>] instance and offers convenient functions
 * to define preferences for common data types like String, Long, Int, Float, Boolean,
 * and Set<String>, as well as custom serialized objects.
 *
 * Features:
 * - Thread-safe operations with mutex locks
 * - In-memory LRU cache for frequently accessed preferences
 * - Batch operations for improved performance
 * - Import/Export functionality
 *
 * @property datastore The underlying [DataStore<Preferences>] instance.
 * @property cacheConfig Configuration for the in-memory cache
 */
@Suppress("unused")
class DatastoreManager(
    internal val datastore: DataStore<Preferences>,
    private val cacheConfig: CacheConfig = CacheConfig(),
) : DatastoreRepository {
    private val cache = if (cacheConfig.enabled) {
        LruCache<String, Any?>(
            maxSize = cacheConfig.maxSize,
            segmentCount = cacheConfig.segmentCount,
            expiryMillis = cacheConfig.expiryMillis,
            recordStats = cacheConfig.recordStats,
        )
    } else {
        null
    }
    private val cacheMutex = Mutex()
    private val batchMutex = Mutex()

    /**
     * Creates a String preference.
     *
     * @param key The preference key.
     * @param defaultValue The default String value.
     * @return A [Prefs] instance for the String preference.
     */
    override fun string(key: String, defaultValue: String): Prefs<String> =
        PrefsImpl(
            StringPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Long preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Long value.
     * @return A [Prefs] instance for the Long preference.
     */
    override fun long(key: String, defaultValue: Long): Prefs<Long> =
        PrefsImpl(
            LongPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates an Int preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Int value.
     * @return A [Prefs] instance for the Int preference.
     */
    override fun int(key: String, defaultValue: Int): Prefs<Int> =
        PrefsImpl(
            IntPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Float preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Float value.
     * @return A [Prefs] instance for the Float preference.
     */
    override fun float(key: String, defaultValue: Float): Prefs<Float> =
        PrefsImpl(
            FloatPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Boolean preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Boolean value.
     * @return A [Prefs] instance for the Boolean preference.
     */
    override fun bool(key: String, defaultValue: Boolean): Prefs<Boolean> =
        PrefsImpl(
            BooleanPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a Set<String> preference.
     *
     * @param key The preference key.
     * @param defaultValue The default Set<String> value.
     * @return A [Prefs] instance for the Set<String> preference.
     */
    override fun stringSet(
        key: String,
        defaultValue: Set<String>,
    ): Prefs<Set<String>> =
        PrefsImpl(
            StringSetPrimitive(
                datastore = datastore,
                key = key,
                defaultValue = defaultValue,
            ),
        )

    /**
     * Creates a preference for a custom object that can be serialized to and deserialized from a String.
     *
     * @param T The type of the custom object.
     * @param key The preference key.
     * @param defaultValue The default value for the custom object.
     * @param serializer A function to serialize the object to a String.
     * @param deserializer A function to deserialize the String back to the object.
     * @return A [Prefs] instance for the custom object preference.
     */
    override fun <T> serialized(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Prefs<T> = PrefsImpl(
        ObjectPrimitive(
            datastore = datastore,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        ),
    )

    override suspend fun export(exportPrivate: Boolean, exportAppState: Boolean): Map<String, JsonElement> {
        return datastore
            .data
            .first()
            .toPreferences()
            .asMap()
            .mapNotNull { (key, values) ->
                if (!exportPrivate && isPrivate(key.name)) {
                    null
                } else if (!exportAppState && isAppState(key.name)) {
                    null
                } else {
                    key.name to values.toJsonElement()
                }
            }
            .toMap()
    }

    override suspend fun import(data: Map<String, Any>) {
        batchMutex.withLock {
            datastore.updateData { currentPreferences ->
                val mutablePreferences = currentPreferences.toMutablePreferences()
                data.forEach { (key, value) ->
                    try {
                        when (value) {
                            is String -> mutablePreferences[stringPreferencesKey(key)] = value

                            is Long -> mutablePreferences[longPreferencesKey(key)] = value

                            is Int -> mutablePreferences[intPreferencesKey(key)] = value

                            is Float -> mutablePreferences[floatPreferencesKey(key)] = value

                            is Boolean -> mutablePreferences[booleanPreferencesKey(key)] = value

                            is Collection<*> -> {
                                if (value.all { it is String }) {
                                    @Suppress("UNCHECKED_CAST")
                                    mutablePreferences[stringSetPreferencesKey(key)] =
                                        (value as Collection<String>).toSet()
                                } else {
                                    // Fallback for mixed-type or non-string collections
                                    val stringValue = value.toJsonElement().toString()
                                    mutablePreferences[stringPreferencesKey(key)] = stringValue
                                }
                            }

                            else -> {
                                // Handle custom objects or unsupported types by serializing them back to a JSON string.
                                val stringValue = when (value) {
                                    is Map<*, *>, is Collection<*> -> value.toJsonElement().toString()
                                    else -> value.toString()
                                }
                                mutablePreferences[stringPreferencesKey(key)] = stringValue
                            }
                        }
                    } catch (e: Exception) {
                        println("$TAG: Error importing key $key: ${e.message}")
                    }
                }
                mutablePreferences.toPreferences()
            }
            // Clear cache after import
            cache?.clear()
        }
    }

    override suspend fun batchSet(operations: Map<Prefs<*>, Any?>) {
        batchMutex.withLock {
            datastore.edit { prefs ->
                operations.forEach { (pref, value) ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        (pref as? Preference<Any?>)?.let { preference ->
                            if (value != null) {
                                // Get the key and set the value directly in preferences
                                val key = preference.key()
                                when (value) {
                                    is String -> prefs[stringPreferencesKey(key)] = value

                                    is Long -> prefs[longPreferencesKey(key)] = value

                                    is Int -> prefs[intPreferencesKey(key)] = value

                                    is Float -> prefs[floatPreferencesKey(key)] = value

                                    is Boolean -> prefs[booleanPreferencesKey(key)] = value

                                    is Set<*> -> {
                                        if (value.all { it is String }) {
                                            @Suppress("UNCHECKED_CAST")
                                            prefs[stringSetPreferencesKey(key)] = value as Set<String>
                                        } else {
                                            println(
                                                "$TAG: Unsupported Set type in batch set for key $key, expected Set<String>",
                                            )
                                        }
                                    }

                                    else -> {
                                        println(
                                            "$TAG: Unsupported value type ${value::class.simpleName} in batch set for key $key",
                                        )
                                    }
                                }
                                // Update cache
                                cache?.let {
                                    cacheMutex.withLock {
                                        it.put(key, value)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("$TAG: Error in batch set for preference ${pref.key()}: ${e.message}")
                    }
                }
            }
        }
    }

    override suspend fun batchGet(preferences: List<Prefs<*>>): Map<Prefs<*>, Any?> {
        val results = mutableMapOf<Prefs<*>, Any?>()
        preferences.forEach { pref ->
            try {
                @Suppress("UNCHECKED_CAST")
                val value = (pref as? Preference<Any?>)?.get()
                results[pref] = value
            } catch (e: Exception) {
                println("$TAG: Error in batch get for preference ${pref.key()}: ${e.message}")
                results[pref] = null
            }
        }
        return results
    }

    override suspend fun batchDelete(preferences: List<Prefs<*>>) {
        batchMutex.withLock {
            datastore.edit { prefs ->
                preferences.forEach { pref ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        (pref as? Preference<Any?>)?.let { preference ->
                            val key = preference.key()
                            // Try the most common types first for efficiency
                            // Most preferences will be one of these types
                            if (prefs.remove(stringPreferencesKey(key)) == null) {
                                if (prefs.remove(intPreferencesKey(key)) == null) {
                                    if (prefs.remove(longPreferencesKey(key)) == null) {
                                        if (prefs.remove(booleanPreferencesKey(key)) == null) {
                                            if (prefs.remove(floatPreferencesKey(key)) == null) {
                                                prefs.remove(stringSetPreferencesKey(key))
                                            }
                                        }
                                    }
                                }
                            }

                            // Remove from cache
                            cache?.remove(key)
                        }
                    } catch (e: Exception) {
                        println("$TAG: Error in batch delete for preference ${pref.key()}: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Clears the in-memory cache.
     * Useful for testing or when you want to force fresh reads from DataStore.
     */
    suspend fun clearCache() {
        cache?.clear()
    }

    /**
     * Gets the current cache statistics.
     * Returns empty statistics if cache is disabled or stats recording is off.
     *
     * @return Current cache statistics including hit/miss rates
     */
    suspend fun getCacheStats(): CacheStats {
        return cache?.getStats() ?: CacheStats()
    }

    /**
     * Performs cache maintenance by removing expired entries.
     * Should be called periodically for caches with expiry enabled.
     * This is a lightweight operation that runs asynchronously.
     */
    suspend fun cleanUpCache() {
        cache?.cleanUp()
    }
}

/**
 * Type alias for backwards compatibility.
 * @deprecated Use DatastoreManager instead
 */
@Deprecated(
    "Use DatastoreManager instead",
    ReplaceWith("DatastoreManager", "io.github.arthurkun.generic.datastore.DatastoreManager"),
)
typealias GenericPreferenceDatastore = DatastoreManager
