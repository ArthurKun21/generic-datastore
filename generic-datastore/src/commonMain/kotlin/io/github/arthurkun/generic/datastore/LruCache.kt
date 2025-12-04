package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min

/**
 * Cache statistics for monitoring performance.
 */
data class CacheStats(
    val hitCount: Long = 0,
    val missCount: Long = 0,
    val loadCount: Long = 0,
    val evictionCount: Long = 0,
) {
    val hitRate: Double
        get() = if (requestCount > 0) hitCount.toDouble() / requestCount else 0.0

    val missRate: Double
        get() = if (requestCount > 0) missCount.toDouble() / requestCount else 0.0

    val requestCount: Long
        get() = hitCount + missCount
}

/**
 * Entry in the cache with access tracking for frequency-based eviction.
 */
private data class CacheEntry<K, V>(
    val key: K,
    val value: V,
    val timestamp: Long = System.currentTimeMillis(),
    var accessCount: Int = 1,
)

/**
 * A high-performance, coroutine-first cache implementation inspired by Caffeine.
 *
 * Features:
 * - Frequency-based eviction using access counting (W-TinyLFU inspired)
 * - Segmented locking for better concurrency
 * - Statistics tracking for monitoring
 * - Coroutine-first design with suspending functions
 * - Time-based expiration support
 *
 * This cache uses a combination of recency (LRU) and frequency (LFU) to make
 * eviction decisions, providing better hit rates than pure LRU.
 *
 * @param K The type of keys maintained by this cache
 * @param V The type of mapped values
 * @property maxSize The maximum number of entries to keep in the cache
 * @property segmentCount Number of segments for concurrent access (default: 4)
 * @property expiryMillis Time in milliseconds after which entries expire (0 = no expiry)
 * @property recordStats Whether to record cache statistics
 */
internal class LruCache<K, V>(
    private val maxSize: Int,
    private val segmentCount: Int = 4,
    private val expiryMillis: Long = 0,
    private val recordStats: Boolean = false,
) {
    // Segment the cache for better concurrency
    private val segments: List<CacheSegment<K, V>>
    private val segmentSize = (maxSize + segmentCount - 1) / segmentCount

    // Statistics
    private var stats = CacheStats()
    private val statsMutex = Mutex()

    init {
        require(maxSize > 0) { "maxSize must be positive" }
        require(segmentCount > 0) { "segmentCount must be positive" }
        require(expiryMillis >= 0) { "expiryMillis must be non-negative" }

        segments = List(segmentCount) { CacheSegment(segmentSize) }
    }

    /**
     * Gets the segment index for a given key.
     */
    private fun segmentFor(key: K): Int {
        return (key.hashCode() and Int.MAX_VALUE) % segmentCount
    }

    /**
     * Gets a value from the cache.
     *
     * @param key The key whose associated value is to be returned
     * @return The value associated with the key, or null if not present or expired
     */
    suspend fun get(key: K): V? {
        val segment = segments[segmentFor(key)]
        val entry = segment.get(key)

        return if (entry != null && !isExpired(entry)) {
            if (recordStats) {
                updateStats { copy(hitCount = hitCount + 1) }
            }
            entry.value
        } else {
            if (recordStats) {
                updateStats { copy(missCount = missCount + 1) }
            }
            if (entry != null && isExpired(entry)) {
                segment.remove(key)
            }
            null
        }
    }

    /**
     * Puts a value into the cache.
     * If the segment exceeds its size, evicts based on frequency and recency.
     *
     * @param key The key with which the specified value is to be associated
     * @param value The value to be associated with the specified key
     */
    suspend fun put(key: K, value: V) {
        val segment = segments[segmentFor(key)]
        val evicted = segment.put(key, value)

        if (recordStats) {
            updateStats {
                copy(
                    loadCount = loadCount + 1,
                    evictionCount = if (evicted) evictionCount + 1 else evictionCount,
                )
            }
        }
    }

    /**
     * Removes a value from the cache.
     *
     * @param key The key whose mapping is to be removed from the cache
     */
    suspend fun remove(key: K) {
        val segment = segments[segmentFor(key)]
        segment.remove(key)
    }

    /**
     * Clears all entries from the cache.
     */
    suspend fun clear() {
        segments.forEach { it.clear() }
        if (recordStats) {
            statsMutex.withLock {
                stats = CacheStats()
            }
        }
    }

    /**
     * Returns the current size of the cache across all segments.
     *
     * @return The total number of entries in the cache
     */
    suspend fun size(): Int {
        return segments.sumOf { it.size() }
    }

    /**
     * Checks if the cache contains a key.
     *
     * @param key The key whose presence in the cache is to be tested
     * @return true if the cache contains the key and it's not expired, false otherwise
     */
    suspend fun containsKey(key: K): Boolean {
        val segment = segments[segmentFor(key)]
        val entry = segment.get(key)
        return entry != null && !isExpired(entry)
    }

    /**
     * Gets the current cache statistics.
     *
     * @return Current cache statistics
     */
    suspend fun getStats(): CacheStats {
        return if (recordStats) {
            statsMutex.withLock { stats }
        } else {
            CacheStats()
        }
    }

    /**
     * Checks if an entry has expired.
     */
    private fun isExpired(entry: CacheEntry<K, V>): Boolean {
        return expiryMillis > 0 && (System.currentTimeMillis() - entry.timestamp) > expiryMillis
    }

    /**
     * Updates statistics atomically.
     */
    private suspend fun updateStats(update: CacheStats.() -> CacheStats) {
        statsMutex.withLock {
            stats = stats.update()
        }
    }

    /**
     * Removes all expired entries from the cache.
     * Should be called periodically for caches with expiry enabled.
     */
    suspend fun cleanUp() {
        if (expiryMillis > 0) {
            segments.forEach { it.removeExpired(expiryMillis) }
        }
    }
}

/**
 * A segment of the cache for concurrent access.
 * Each segment is independently locked for better concurrency.
 */
private class CacheSegment<K, V>(private val maxSize: Int) {
    private val cache = LinkedHashMap<K, CacheEntry<K, V>>(maxSize, 0.75f, false)
    private val mutex = Mutex()

    suspend fun get(key: K): CacheEntry<K, V>? = mutex.withLock {
        cache[key]?.also { entry ->
            // Increment access count for frequency tracking
            entry.accessCount++
        }
    }

    suspend fun put(key: K, value: V): Boolean = mutex.withLock {
        val entry = CacheEntry(key, value)
        cache[key] = entry

        var evicted = false
        if (cache.size > maxSize) {
            // Evict based on frequency and recency
            val victim = selectVictim()
            if (victim != null) {
                cache.remove(victim)
                evicted = true
            }
        }
        evicted
    }

    suspend fun remove(key: K) = mutex.withLock {
        cache.remove(key)
    }

    suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    suspend fun size(): Int = mutex.withLock {
        cache.size
    }

    suspend fun removeExpired(expiryMillis: Long) = mutex.withLock {
        val now = System.currentTimeMillis()
        val toRemove = cache.values.filter { (now - it.timestamp) > expiryMillis }
            .map { it.key }
        toRemove.forEach { cache.remove(it) }
    }

    /**
     * Selects a victim for eviction using W-TinyLFU inspired approach.
     * Samples a few entries and evicts the one with lowest access count.
     * If tied, evicts the oldest.
     */
    private fun selectVictim(): K? {
        if (cache.isEmpty()) return null

        // Sample size: 4-8 entries for good balance between accuracy and performance
        val sampleSize = min(8, cache.size)
        val samples = cache.values.shuffled().take(sampleSize)

        // Find entry with minimum access count, break ties by timestamp (oldest first)
        return samples.minWithOrNull(
            compareBy<CacheEntry<K, V>> { it.accessCount }
                .thenBy { it.timestamp },
        )?.key
    }
}
