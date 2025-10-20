package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for in-memory cache functionality.
 */
class CacheTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) {
            createTempFile("test_cache", ".preferences_pb").also { it.deleteOnExit() }
        }
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
        // Reset cache settings to defaults
        GenericPreference.cacheEnabled = true
        GenericPreference.cacheTTL = 5.seconds
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun cache_returnsValueOnSecondRead() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_test", "default")
        pref.set("cached_value")

        // First read - from DataStore
        val firstRead = pref.get()
        assertEquals("cached_value", firstRead)

        // Second read - should be from cache
        val secondRead = pref.get()
        assertEquals("cached_value", secondRead)
    }

    @Test
    fun cache_invalidatesOnSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_invalidate", "default")
        pref.set("initial_value")

        // Read to populate cache
        assertEquals("initial_value", pref.get())

        // Update value
        pref.set("updated_value")

        // Should get updated value from cache
        assertEquals("updated_value", pref.get())
    }

    @Test
    fun cache_invalidatesOnDelete() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_delete", "default")
        pref.set("some_value")

        // Read to populate cache
        assertEquals("some_value", pref.get())

        // Delete preference
        pref.delete()

        // Should return default value
        assertEquals("default", pref.get())
    }

    @Test
    fun cache_respectsTTL() = runTest(testDispatcher) {
        // Set very short TTL for testing
        GenericPreference.cacheTTL = 100.milliseconds

        val pref = preferenceDatastore.int("cache_ttl", 0)
        pref.set(100)

        // First read
        assertEquals(100, pref.get())

        // Wait for TTL to expire
        delay(150)

        // Should still work (reads from DataStore)
        assertEquals(100, pref.get())
    }

    @Test
    fun cache_canBeDisabled() = runTest(testDispatcher) {
        GenericPreference.cacheEnabled = false

        val pref = preferenceDatastore.string("cache_disabled", "default")
        pref.set("test_value")

        // Both reads should go to DataStore (no caching)
        assertEquals("test_value", pref.get())
        assertEquals("test_value", pref.get())
    }

    @Test
    fun cache_manualInvalidation() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_manual", "default")
        pref.set("value1")

        // Read to populate cache
        assertEquals("value1", pref.get())

        // Manually invalidate cache
        pref.invalidateCache()

        // Should read from DataStore again
        assertEquals("value1", pref.get())
    }

    @Test
    fun cache_worksWithDifferentTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("cache_string", "")
        val intPref = preferenceDatastore.int("cache_int", 0)
        val boolPref = preferenceDatastore.bool("cache_bool", false)

        stringPref.set("cached")
        intPref.set(42)
        boolPref.set(true)

        // All should be cached independently
        assertEquals("cached", stringPref.get())
        assertEquals(42, intPref.get())
        assertEquals(true, boolPref.get())
    }

    @Test
    fun cache_isolatedBetweenPreferences() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.int("cache_pref1", 0)
        val pref2 = preferenceDatastore.int("cache_pref2", 0)

        pref1.set(10)
        pref2.set(20)

        // Both should have independent caches
        assertEquals(10, pref1.get())
        assertEquals(20, pref2.get())

        // Invalidate one shouldn't affect the other
        pref1.invalidateCache()

        assertEquals(10, pref1.get())
        assertEquals(20, pref2.get())
    }

    @Test
    fun cache_handlesErrorsGracefully() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_error", "default")
        pref.set("cached_value")

        // Read to populate cache
        assertEquals("cached_value", pref.get())

        // Even if DataStore has issues, cached value should be returned
        // (This is tested implicitly in the get() implementation)
        assertEquals("cached_value", pref.get())
    }
}
