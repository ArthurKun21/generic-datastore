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
import io.github.arthurkun.generic.datastore.cache.CaffeineCache
import io.github.arthurkun.generic.datastore.cache.caffeineCache
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Represents a generic preference that can be stored in and retrieved from a DataStore.
 *
 * This sealed class provides a type-safe way to handle different preference types (String, Int, Long, etc.)
 * while abstracting the underlying DataStore operations. It defines common operations for a preference,
 * such as reading, writing, deleting, and observing its value as a Kotlin Flow or StateFlow.
 *
 * Each specific preference type (e.g., [StringPrimitive], [IntPrimitive]) is implemented as a nested class
 * inheriting from [GenericPreference].
 *
 * @param T The data type of the preference value (e.g., String, Int, Boolean).
 * @property datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
 * @property key The unique String key used to identify this preference within the DataStore.
 * @property defaultValue The default value to be returned if the preference is not set or an error occurs.
 * @property preferences The [Preferences.Key] specific to the type `T`, used to access the preference in DataStore.
 */
sealed class GenericPreference<T>(
    internal val datastore: DataStore<Preferences>,
    protected val key: String,
    override val defaultValue: T,
    private val preferences: Preferences.Key<T>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Preference<T> {

    /**
     * Mutex for synchronizing concurrent access to blocking getValue/setValue operations.
     * This ensures thread-safety when multiple threads access the same preference simultaneously.
     */
    private val accessMutex = Mutex()

    /**
     * Cache configuration for controlling cache behavior.
     */
    companion object {
        /**
         * Global high-performance Caffeine-inspired cache for all preferences.
         * Features: LRU eviction, TTL/idle timeout, statistics tracking.
         */
        private var globalCache: CaffeineCache<Any>? = null

        /**
         * Time-to-live for cache entries. After this duration, cached values are considered stale
         * and will be refreshed from DataStore on next access.
         * Default: 5 minutes
         */
        var cacheTTL: Duration = 5.minutes
            set(value) {
                field = value
                rebuildCache()
            }

        /**
         * Maximum number of entries in the cache. When exceeded, LRU entries are evicted.
         * Default: 10,000
         */
        var cacheMaxSize: Long = 10_000
            set(value) {
                field = value
                rebuildCache()
            }

        /**
         * Idle timeout after last access. Null for no idle timeout.
         * Default: null (no idle timeout)
         */
        var cacheIdleTimeout: Duration? = null
            set(value) {
                field = value
                rebuildCache()
            }

        /**
         * Enables or disables the cache. When disabled, all reads go directly to DataStore.
         * Default: true
         */
        var cacheEnabled: Boolean = true

        /**
         * Gets cache statistics for monitoring performance.
         */
        fun cacheStats(): CaffeineCache.Stats = runBlocking {
            getCache().stats()
        }

        /**
         * Manually triggers cache cleanup to remove expired entries.
         */
        fun cleanUpCache() = runBlocking {
            getCache().cleanUp()
        }

        /**
         * Invalidates all cache entries across all preferences.
         */
        fun invalidateAllCaches() = runBlocking {
            getCache().invalidateAll()
        }

        /**
         * Gets or creates the global cache instance.
         */
        @Suppress("UNCHECKED_CAST")
        private fun getCache(): CaffeineCache<Any> {
            if (globalCache == null) {
                globalCache = caffeineCache<Any>()
                    .maximumSize(cacheMaxSize)
                    .expireAfterWrite(cacheTTL)
                    .apply {
                        cacheIdleTimeout?.let { expireAfterAccess(it) }
                    }
                    .build()
            }
            return globalCache!!
        }

        /**
         * Rebuilds the cache with new configuration.
         */
        private fun rebuildCache() {
            globalCache = null
        }
    }

    /**
     * Returns the unique String key used to identify this preference within the DataStore.
     */
    override fun key(): String = key

    /**
     * Invalidates the cache for this preference.
     * Call this method when you know the value has changed externally.
     */
    fun invalidateCache() = runBlocking {
        if (cacheEnabled) {
            getCache().invalidate(key)
        }
    }

    /**
     * Retrieves the current value of the preference from DataStore or cache.
     * If the key is not found in DataStore or an error occurs during retrieval,
     * this function returns the [defaultValue]. This is a suspending function.
     *
     * Caching behavior:
     * - If cache is enabled and valid, returns cached value without DataStore access
     * - Otherwise, fetches from DataStore and updates cache
     * - Uses Caffeine-inspired cache with LRU eviction and TTL
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun get(): T {
        // Check cache first if enabled
        if (cacheEnabled) {
            val cached = getCache().get(key) as? T
            if (cached != null) {
                return cached
            }
        }

        return withContext(ioDispatcher) {
            try {
                val value = datastore
                    .data
                    .map { preferences ->
                        preferences[this@GenericPreference.preferences] ?: defaultValue
                    }
                    .first()

                // Update cache
                if (cacheEnabled) {
                    getCache().put(key, value as Any)
                }

                value
            } catch (e: Exception) {
                ConsoleLogger.error("Failed to get value for key '$key'", e)
                // Try to return cached value if available, otherwise default
                val cached = if (cacheEnabled) getCache().get(key) as? T else null
                cached ?: defaultValue
            }
        }
    }

    /**
     * Sets the value of the preference in the DataStore.
     * This is a suspending function.
     * Automatically updates the cache after successful write.
     * @param value The new value to store for this preference.
     */
    override suspend fun set(value: T) {
        withContext(ioDispatcher) {
            try {
                datastore.edit { ds ->
                    ds[preferences] = value
                }
                // Update cache immediately after successful write
                if (cacheEnabled) {
                    getCache().put(key, value as Any)
                }
            } catch (e: Exception) {
                ConsoleLogger.error("Failed to set value for key '$key'", e)
            }
        }
    }

    /**
     * Removes the preference from the DataStore.
     * This is a suspending function.
     * Automatically invalidates the cache after deletion.
     */
    override suspend fun delete() {
        withContext(ioDispatcher) {
            try {
                datastore.edit { ds ->
                    ds.remove(preferences)
                }
                // Invalidate cache after deletion
                if (cacheEnabled) {
                    getCache().invalidate(key)
                }
            } catch (e: Exception) {
                ConsoleLogger.error("Failed to delete value for key '$key'", e)
            }
        }
    }

    /**
     * Returns a [Flow] that emits the preference's current value and subsequent updates from DataStore.
     * If the preference is not set in the DataStore or an error occurs during retrieval,
     * the flow will emit the [defaultValue].
     */
    override fun asFlow(): Flow<T> {
        return datastore
            .data
            .map { preferences ->
                preferences[this.preferences] ?: defaultValue
            }
    }

    /**
     * Converts the preference [Flow] into a [StateFlow] within the given [scope].
     * The [StateFlow] is typically started when there are subscribers and shares the most recent value.
     * It will be initialized with the current preference value (or [defaultValue] if not set or on error).
     * @param scope The [CoroutineScope] in which to launch the [StateFlow].
     * @return A [StateFlow] representing the preference's value.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> =
        asFlow().stateIn(scope, SharingStarted.Eagerly, defaultValue)

    /**
     * Synchronously retrieves the current value of the preference.
     * This operation may block the calling thread while accessing DataStore.
     * If the key is not found or an error occurs, this function returns the [defaultValue].
     * Thread-safe: Uses mutex to synchronize concurrent access from multiple threads.
     * Use with caution due to potential blocking.
     */
    override fun getValue(): T = runBlocking {
        accessMutex.withLock {
            get()
        }
    }

    /**
     * Synchronously sets the value of the preference.
     * This operation may block the calling thread while accessing DataStore.
     * Thread-safe: Uses mutex to synchronize concurrent access from multiple threads.
     * Use with caution due to potential blocking.
     * @param value The new value to store for this preference.
     */
    override fun setValue(value: T) {
        runBlocking {
            accessMutex.withLock {
                set(value)
            }
        }
    }

    /**
     * A [GenericPreference] for storing [String] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class StringPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: String,
    ) : GenericPreference<String>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = stringPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Long] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class LongPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Long,
    ) : GenericPreference<Long>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = longPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Int] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class IntPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Int,
    ) : GenericPreference<Int>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = intPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Float] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class FloatPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Float,
    ) : GenericPreference<Float>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = floatPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Boolean] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class BooleanPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Boolean,
    ) : GenericPreference<Boolean>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = booleanPreferencesKey(key),
    )

    /**
     * A [GenericPreference] for storing [Set] of [String] values.
     * @param datastore The [DataStore<Preferences>] instance used for storing and retrieving preferences.
     * @param key The unique String key used to identify this preference within the DataStore.
     * @param defaultValue The default value to use if the preference is not set or on retrieval error.
     */
    class StringSetPrimitive(
        datastore: DataStore<Preferences>,
        key: String,
        defaultValue: Set<String>,
    ) : GenericPreference<Set<String>>(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        preferences = stringSetPreferencesKey(key),
    )
}
