package io.github.arthurkun.generic.datastore.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

/**
 * A high-performance, coroutines-first cache inspired by Caffeine cache.
 *
 * Features:
 * - Size-based eviction with LRU policy
 * - Time-based expiration (TTL and idle timeout)
 * - Thread-safe concurrent access using coroutines
 * - Statistics tracking (hits, misses, evictions)
 * - Efficient memory management
 *
 * @param T The type of values stored in the cache
 * @property maxSize Maximum number of entries in the cache. When exceeded, LRU entries are evicted.
 * @property expireAfterWrite Time-to-live after an entry is written. Null for no TTL.
 * @property expireAfterAccess Idle timeout after last access. Null for no idle timeout.
 */
class CaffeineCache<T>(
    private val maxSize: Long = 10_000,
    private val expireAfterWrite: Duration? = 5.minutes,
    private val expireAfterAccess: Duration? = null,
) {

    /**
     * Cache entry with metadata for eviction and expiration.
     */
    private data class CacheEntry<T>(
        val value: T,
        val writeTime: TimeSource.Monotonic.ValueTimeMark,
        var accessTime: TimeSource.Monotonic.ValueTimeMark,
        var accessCount: Long = 0,
    )

    /**
     * Cache statistics for monitoring.
     */
    data class Stats(
        val hitCount: Long = 0,
        val missCount: Long = 0,
        val evictionCount: Long = 0,
        val size: Int = 0,
    ) {
        val hitRate: Double get() = if (requestCount > 0) hitCount.toDouble() / requestCount else 0.0
        val missRate: Double get() = if (requestCount > 0) missCount.toDouble() / requestCount else 0.0
        val requestCount: Long get() = hitCount + missCount
    }

    /**
     * Internal storage using LinkedHashMap for LRU access order.
     * LinkedHashMap maintains insertion/access order and allows efficient LRU implementation.
     */
    private val cache = LinkedHashMap<String, CacheEntry<T>>(16, 0.75f, true)

    /**
     * Mutex for thread-safe concurrent access.
     */
    private val mutex = Mutex()

    /**
     * Statistics counters.
     */
    @Volatile
    private var hitCount = 0L

    @Volatile
    private var missCount = 0L

    @Volatile
    private var evictionCount = 0L

    /**
     * Gets a value from the cache.
     * Returns null if the key doesn't exist or the entry has expired.
     *
     * This is a suspending function to ensure thread-safe access via mutex.
     *
     * @param key The cache key
     * @return The cached value or null if not found or expired
     */
    suspend fun get(key: String): T? = mutex.withLock {
        val entry = cache[key]

        if (entry == null) {
            missCount++
            return null
        }

        // Check if entry has expired
        if (isExpired(entry)) {
            cache.remove(key)
            evictionCount++
            missCount++
            return null
        }

        // Update access time and count
        entry.accessTime = TimeSource.Monotonic.markNow()
        entry.accessCount++
        hitCount++

        return entry.value
    }

    /**
     * Puts a value into the cache.
     * If the cache is full, evicts the least recently used entry.
     *
     * @param key The cache key
     * @param value The value to cache
     */
    suspend fun put(key: String, value: T) = mutex.withLock {
        val now = TimeSource.Monotonic.markNow()

        // Create new cache entry
        val entry = CacheEntry(
            value = value,
            writeTime = now,
            accessTime = now,
            accessCount = 0,
        )

        cache[key] = entry

        // Evict if over max size
        if (cache.size > maxSize) {
            evictOldestEntry()
        }
    }

    /**
     * Invalidates a specific cache entry.
     *
     * @param key The cache key to invalidate
     */
    suspend fun invalidate(key: String) = mutex.withLock {
        if (cache.remove(key) != null) {
            evictionCount++
        }
    }

    /**
     * Invalidates all cache entries.
     */
    suspend fun invalidateAll() = mutex.withLock {
        evictionCount += cache.size
        cache.clear()
    }

    /**
     * Removes expired entries from the cache.
     * Should be called periodically for maintenance.
     */
    suspend fun cleanUp() = mutex.withLock {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (isExpired(entry.value)) {
                iterator.remove()
                evictionCount++
            }
        }
    }

    /**
     * Gets current cache statistics.
     */
    suspend fun stats(): Stats = mutex.withLock {
        Stats(
            hitCount = hitCount,
            missCount = missCount,
            evictionCount = evictionCount,
            size = cache.size,
        )
    }

    /**
     * Gets the current cache size.
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }

    /**
     * Checks if an entry has expired based on write time or access time.
     */
    private fun isExpired(entry: CacheEntry<T>): Boolean {
        val now = TimeSource.Monotonic.markNow()

        // Check write-based expiration (TTL)
        expireAfterWrite?.let { ttl ->
            if (entry.writeTime.elapsedNow() >= ttl) {
                return true
            }
        }

        // Check access-based expiration (idle timeout)
        expireAfterAccess?.let { timeout ->
            if (entry.accessTime.elapsedNow() >= timeout) {
                return true
            }
        }

        return false
    }

    /**
     * Evicts the oldest (least recently used) entry from the cache.
     * LinkedHashMap in access-order mode maintains entries in LRU order,
     * so the first entry is the least recently used.
     */
    private fun evictOldestEntry() {
        val iterator = cache.iterator()
        if (iterator.hasNext()) {
            iterator.next()
            iterator.remove()
            evictionCount++
        }
    }
}

/**
 * Builder for creating CaffeineCache instances with fluent API.
 */
class CaffeineCacheBuilder<T> {
    private var maxSize: Long = 10_000
    private var expireAfterWrite: Duration? = 5.minutes
    private var expireAfterAccess: Duration? = null

    /**
     * Sets the maximum size of the cache.
     */
    fun maximumSize(size: Long) = apply {
        require(size > 0) { "Maximum size must be positive" }
        this.maxSize = size
    }

    /**
     * Sets the time-to-live after write.
     */
    fun expireAfterWrite(duration: Duration) = apply {
        this.expireAfterWrite = duration
    }

    /**
     * Sets the idle timeout after last access.
     */
    fun expireAfterAccess(duration: Duration) = apply {
        this.expireAfterAccess = duration
    }

    /**
     * Builds the cache instance.
     */
    fun build(): CaffeineCache<T> {
        return CaffeineCache(
            maxSize = maxSize,
            expireAfterWrite = expireAfterWrite,
            expireAfterAccess = expireAfterAccess,
        )
    }
}

/**
 * Creates a new CaffeineCacheBuilder for type T.
 */
fun <T> caffeineCache(): CaffeineCacheBuilder<T> = CaffeineCacheBuilder()
