package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TEST_DATASTORE_NAME = "test_caffeine_cache"

/**
 * Tests for the Caffeine-inspired cache implementation.
 * These tests verify the high-performance features like:
 * - Segmented locking for better concurrency
 * - Frequency-based eviction
 * - Statistics tracking
 * - Expiration support
 */
class CaffeineInspiredCacheTest {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var dataStore: DataStore<Preferences>
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                File(tempFolder, "${TEST_DATASTORE_NAME}.preferences_pb")
            },
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun testCacheStatistics() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(
                enabled = true,
                maxSize = 10,
                recordStats = true,
            ),
        )

        // Use batch operations which utilize the cache
        val pref1 = datastoreManager.string("stats1", "default")
        val pref2 = datastoreManager.string("stats2", "default")

        // Set values using batch operations
        datastoreManager.batchSet(
            mapOf(
                pref1 to "value1",
                pref2 to "value2",
            ),
        )

        // Get stats
        val stats = datastoreManager.getCacheStats()

        // Verify statistics are being tracked
        assertTrue(stats.loadCount >= 2, "Load count should be at least 2")
        assertTrue(stats.hitRate >= 0.0 && stats.hitRate <= 1.0, "Hit rate should be between 0 and 1")
    }

    @Test
    fun testSegmentedConcurrency() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(
                enabled = true,
                maxSize = 100,
                segmentCount = 8, // Higher segment count for better concurrency
            ),
        )

        // Create many preferences that will be distributed across segments
        val prefs = (1..50).map { i ->
            datastoreManager.int("concurrent$i", 0)
        }

        // Perform concurrent operations
        val jobs = prefs.map { pref ->
            async {
                pref.set(42)
                pref.get()
            }
        }

        // Wait for all to complete
        jobs.forEach { it.await() }

        // Verify all values are set correctly
        prefs.forEach { pref ->
            assertEquals(42, pref.get())
        }
    }

    @Test
    fun testFrequencyBasedEviction() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(
                enabled = true,
                maxSize = 5,
                recordStats = true,
            ),
        )

        // Create more preferences than cache size to trigger eviction
        val prefs = (1..10).map { datastoreManager.string("evict$it", "default") }

        // Use batch operations to populate cache
        val operations: Map<Prefs<*>, Any?> = prefs.associateWith { "value" }
        datastoreManager.batchSet(operations)

        // Get stats
        val stats = datastoreManager.getCacheStats()
        
        // With maxSize=5 and 10 items, we should see evictions
        assertTrue(stats.evictionCount > 0, "Some evictions should have occurred")
        assertTrue(stats.loadCount == 10L, "All 10 items should have been loaded")
    }

    @Test
    fun testCacheExpiration() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(
                enabled = true,
                maxSize = 10,
                expiryMillis = 100, // 100ms expiry
            ),
        )

        val pref = datastoreManager.string("expiry-test", "default")

        // Set a value
        pref.set("value")
        assertEquals("value", pref.get())

        // Wait for expiry
        delay(150)

        // Clean up expired entries
        datastoreManager.cleanUpCache()

        // Value should still be accessible from DataStore, not cache
        assertEquals("value", pref.get())
    }

    @Test
    fun testCacheClearing() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(
                enabled = true,
                maxSize = 10,
                recordStats = true,
            ),
        )

        val pref = datastoreManager.string("clear-test", "default")

        // Use batch operations to populate cache
        datastoreManager.batchSet(mapOf(pref to "value"))

        val statsBefore = datastoreManager.getCacheStats()
        assertTrue(statsBefore.loadCount > 0, "Some loads should have occurred")

        // Clear cache
        datastoreManager.clearCache()

        val statsAfter = datastoreManager.getCacheStats()
        assertEquals(0, statsAfter.requestCount, "Stats should be reset after cache clear")
        assertEquals(0, statsAfter.loadCount, "Load count should be reset")
    }

    @Test
    fun testMultipleSegments() = runTest(testDispatcher) {
        // Test with different segment counts
        for (segmentCount in listOf(1, 2, 4, 8)) {
            val localDataStore = PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = {
                    File(tempFolder, "test_segments_$segmentCount.preferences_pb")
                },
            )

            val datastoreManager = DatastoreManager(
                localDataStore,
                CacheConfig(
                    enabled = true,
                    maxSize = 20,
                    segmentCount = segmentCount,
                ),
            )

            val prefs = (1..20).map { i ->
                datastoreManager.int("seg${segmentCount}_pref$i", 0)
            }

            // Set all values
            prefs.forEachIndexed { index, pref ->
                pref.set(index)
            }

            // Verify all values
            prefs.forEachIndexed { index, pref ->
                assertEquals(index, pref.get())
            }
        }
    }

    @Test
    fun testCacheWithNoStats() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(
                enabled = true,
                maxSize = 10,
                recordStats = false, // Stats disabled
            ),
        )

        val pref = datastoreManager.string("no-stats", "default")
        pref.set("value")
        pref.get()

        val stats = datastoreManager.getCacheStats()
        assertEquals(0, stats.requestCount, "Stats should be empty when recording is disabled")
    }

    @Test
    fun testCacheDisabled() = runTest(testDispatcher) {
        val datastoreManager = DatastoreManager(
            dataStore,
            CacheConfig(enabled = false),
        )

        val pref = datastoreManager.string("disabled-cache", "default")
        pref.set("value")

        // Should still work, just without caching
        assertEquals("value", pref.get())

        // Stats should be empty
        val stats = datastoreManager.getCacheStats()
        assertEquals(0, stats.requestCount)
    }
}
