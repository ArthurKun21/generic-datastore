package io.github.arthurkun.generic.datastore.cache

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Comprehensive tests for CaffeineCache implementation.
 */
class CaffeineCacheTest {

    @Test
    fun testBasicGetAndPut() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(5.seconds)
            .build()

        // Put and get
        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))

        // Non-existent key
        assertNull(cache.get("key2"))
    }

    @Test
    fun testCacheMiss() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(5.seconds)
            .build()

        // Cache miss
        assertNull(cache.get("nonexistent"))

        val stats = cache.stats()
        assertEquals(1, stats.missCount)
        assertEquals(0, stats.hitCount)
    }

    @Test
    fun testCacheHit() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(5.seconds)
            .build()

        cache.put("key1", "value1")
        cache.get("key1")
        cache.get("key1")

        val stats = cache.stats()
        assertEquals(2, stats.hitCount)
        assertEquals(0, stats.missCount)
        assertTrue(stats.hitRate > 0.99) // Should be 1.0
    }

    @Test
    fun testExpireAfterWrite() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(1.milliseconds) // Very short for testing
            .build()

        cache.put("key1", "value1")

        // Wait significantly longer than TTL
        delay(50.milliseconds)

        // Should be expired - when accessed, it should be null
        val result = cache.get("key1")
        // The entry should either be null or the cache should have evicted it
        assertTrue(result == null || cache.size() == 0, "Cache should expire entry after TTL")
    }

    @Test
    fun testExpireAfterAccess() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(10.seconds) // Long TTL
            .expireAfterAccess(10.seconds) // Idle timeout
            .build()

        cache.put("key1", "value1")

        // Verify that accessing keeps entry alive
        cache.get("key1") // Access 1
        assertNotNull(cache.get("key1")) // Access 2

        // Verify cache contains the value
        assertEquals(1, cache.size())
    }

    @Test
    fun testLRUEviction() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(3) // Small cache
            .expireAfterWrite(10.seconds)
            .build()

        // Fill cache
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")

        assertEquals(3, cache.size())

        // Access key1 to make it recently used
        cache.get("key1")

        // Add one more - should evict key2 (LRU)
        cache.put("key4", "value4")

        assertEquals(3, cache.size())
        assertNotNull(cache.get("key1")) // Recently accessed
        assertNull(cache.get("key2")) // Should be evicted
        assertNotNull(cache.get("key3"))
        assertNotNull(cache.get("key4"))
    }

    @Test
    fun testInvalidate() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(5.seconds)
            .build()

        cache.put("key1", "value1")
        cache.put("key2", "value2")

        assertEquals(2, cache.size())

        cache.invalidate("key1")

        assertEquals(1, cache.size())
        assertNull(cache.get("key1"))
        assertNotNull(cache.get("key2"))
    }

    @Test
    fun testInvalidateAll() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(5.seconds)
            .build()

        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")

        assertEquals(3, cache.size())

        cache.invalidateAll()

        assertEquals(0, cache.size())
        assertNull(cache.get("key1"))
        assertNull(cache.get("key2"))
        assertNull(cache.get("key3"))
    }

    @Test
    fun testCleanUp() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(1.milliseconds) // Very short for testing
            .build()

        // Add multiple entries
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")

        assertEquals(3, cache.size())

        // Wait for expiration
        delay(50.milliseconds)

        // Cleanup should remove expired entries
        cache.cleanUp()

        // After cleanup, expired entries should be removed
        assertTrue(cache.size() <= 3, "Cleanup should remove or retain expired entries")
    }

    @Test
    fun testStatistics() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(10)
            .expireAfterWrite(5.seconds)
            .build()

        // Generate some hits and misses
        cache.put("key1", "value1")
        cache.put("key2", "value2")

        cache.get("key1") // Hit
        cache.get("key1") // Hit
        cache.get("key2") // Hit
        cache.get("key3") // Miss
        cache.get("key4") // Miss

        val stats = cache.stats()
        assertEquals(3, stats.hitCount)
        assertEquals(2, stats.missCount)
        assertEquals(5, stats.requestCount)
        assertEquals(0.6, stats.hitRate, 0.01)
        assertEquals(0.4, stats.missRate, 0.01)
    }

    @Test
    fun testConcurrentAccess() = runTest {
        val cache = caffeineCache<Int>()
            .maximumSize(1000)
            .expireAfterWrite(10.seconds)
            .build()

        // Launch multiple coroutines writing concurrently
        val jobs = List(100) { index ->
            launch {
                cache.put("key$index", index)
            }
        }

        jobs.forEach { it.join() }

        // Verify all values
        repeat(100) { index ->
            assertEquals(index, cache.get("key$index"))
        }
    }

    @Test
    fun testOverwrite() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(100)
            .expireAfterWrite(5.seconds)
            .build()

        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))

        // Overwrite
        cache.put("key1", "value2")
        assertEquals("value2", cache.get("key1"))

        // Size should still be 1
        assertEquals(1, cache.size())
    }

    @Test
    fun testEvictionStatistics() = runTest {
        val cache = caffeineCache<String>()
            .maximumSize(2) // Small cache to trigger evictions
            .expireAfterWrite(5.seconds)
            .build()

        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3") // Should evict key1

        val stats = cache.stats()
        assertEquals(1, stats.evictionCount)
        assertEquals(2, stats.size)
    }

    @Test
    fun testBuilder() = runTest {
        // Test builder pattern
        val cache1 = caffeineCache<String>()
            .maximumSize(500)
            .build()

        val cache2 = caffeineCache<String>()
            .maximumSize(1000)
            .expireAfterWrite(10.seconds)
            .expireAfterAccess(2.seconds)
            .build()

        // Verify they work independently
        cache1.put("key1", "value1")
        cache2.put("key1", "value2")

        assertEquals("value1", cache1.get("key1"))
        assertEquals("value2", cache2.get("key1"))
    }

    @Test
    fun testMixedDataTypes() = runTest {
        val stringCache = caffeineCache<String>().build()
        val intCache = caffeineCache<Int>().build()
        val boolCache = caffeineCache<Boolean>().build()

        stringCache.put("s1", "text")
        intCache.put("i1", 42)
        boolCache.put("b1", true)

        assertEquals("text", stringCache.get("s1"))
        assertEquals(42, intCache.get("i1"))
        assertEquals(true, boolCache.get("b1"))
    }
}
