package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A thread-safe Least Recently Used (LRU) cache implementation.
 *
 * This cache maintains a maximum size and evicts the least recently used entries
 * when the size limit is reached.
 *
 * @param K The type of keys maintained by this cache
 * @param V The type of mapped values
 * @property maxSize The maximum number of entries to keep in the cache
 */
internal class LruCache<K, V>(private val maxSize: Int) {
    private val cache = LinkedHashMap<K, V>(maxSize, 0.75f, true)
    private val mutex = Mutex()

    /**
     * Gets a value from the cache.
     *
     * @param key The key whose associated value is to be returned
     * @return The value associated with the key, or null if not present
     */
    suspend fun get(key: K): V? = mutex.withLock {
        cache[key]
    }

    /**
     * Puts a value into the cache.
     * If the cache exceeds maxSize, the least recently used entry is removed.
     *
     * @param key The key with which the specified value is to be associated
     * @param value The value to be associated with the specified key
     */
    suspend fun put(key: K, value: V) = mutex.withLock {
        cache[key] = value
        if (cache.size > maxSize) {
            val eldest = cache.entries.iterator().next()
            cache.remove(eldest.key)
        }
    }

    /**
     * Removes a value from the cache.
     *
     * @param key The key whose mapping is to be removed from the cache
     */
    suspend fun remove(key: K) = mutex.withLock {
        cache.remove(key)
    }

    /**
     * Clears all entries from the cache.
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    /**
     * Returns the current size of the cache.
     *
     * @return The number of entries in the cache
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }

    /**
     * Checks if the cache contains a key.
     *
     * @param key The key whose presence in the cache is to be tested
     * @return true if the cache contains the key, false otherwise
     */
    suspend fun containsKey(key: K): Boolean = mutex.withLock {
        cache.containsKey(key)
    }
}
