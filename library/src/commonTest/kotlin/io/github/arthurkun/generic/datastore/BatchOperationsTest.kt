package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Tests for batch operations (batchGet, batchSet, batchDelete).
 */
class BatchOperationsTest {

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
            createTempFile("test_batch", ".preferences_pb").also { it.deleteOnExit() }
        }
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
        // Reset cache settings
        GenericPreference.cacheEnabled = true
        GenericPreference.cacheTTL = 5.seconds
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun batchGet_retrievesMultiplePreferences() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("batch_get_1", "default1")
        val pref2 = preferenceDatastore.string("batch_get_2", "default2")
        val pref3 = preferenceDatastore.string("batch_get_3", "default3")

        // Set some values
        pref1.set("value1")
        pref2.set("value2")
        pref3.set("value3")

        // Batch get
        val results = preferenceDatastore.batchGet(listOf(pref1, pref2, pref3))

        assertEquals(3, results.size)
        assertEquals("value1", results["batch_get_1"])
        assertEquals("value2", results["batch_get_2"])
        assertEquals("value3", results["batch_get_3"])
    }

    @Test
    fun batchGet_handlesEmptyList() = runTest(testDispatcher) {
        val results = preferenceDatastore.batchGet(emptyList<Prefs<String>>())
        assertEquals(0, results.size)
    }

    @Test
    fun batchGet_returnsDefaultsForUnsetPreferences() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("unset_1", "default1")
        val pref2 = preferenceDatastore.string("unset_2", "default2")

        val results = preferenceDatastore.batchGet(listOf(pref1, pref2))

        assertEquals("default1", results["unset_1"])
        assertEquals("default2", results["unset_2"])
    }

    @Test
    fun batchSet_updatesMultiplePreferences() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("batch_set_string", "")
        val intPref = preferenceDatastore.int("batch_set_int", 0)
        val boolPref = preferenceDatastore.bool("batch_set_bool", false)

        // Batch set
        preferenceDatastore.batchSet(
            mapOf(
                stringPref to "updated_string",
                intPref to 42,
                boolPref to true,
            ),
        )

        // Verify values were set
        assertEquals("updated_string", stringPref.get())
        assertEquals(42, intPref.get())
        assertEquals(true, boolPref.get())
    }

    @Test
    fun batchSet_handlesEmptyMap() = runTest(testDispatcher) {
        // Should not throw
        preferenceDatastore.batchSet(emptyMap())
    }

    @Test
    fun batchSet_invalidatesCache() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_invalidate_batch", "initial")
        pref.set("initial")

        // Populate cache
        assertEquals("initial", pref.get())

        // Batch set should invalidate cache
        preferenceDatastore.batchSet(mapOf(pref to "updated"))

        // Should get updated value (cache was invalidated)
        assertEquals("updated", pref.get())
    }

    @Test
    fun batchSet_handlesAllPrimitiveTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("batch_string", "")
        val intPref = preferenceDatastore.int("batch_int", 0)
        val longPref = preferenceDatastore.long("batch_long", 0L)
        val floatPref = preferenceDatastore.float("batch_float", 0f)
        val boolPref = preferenceDatastore.bool("batch_bool", false)
        val stringSetPref = preferenceDatastore.stringSet("batch_set", emptySet())

        preferenceDatastore.batchSet(
            mapOf(
                stringPref to "test",
                intPref to 100,
                longPref to 999L,
                floatPref to 3.14f,
                boolPref to true,
                stringSetPref to setOf("a", "b", "c"),
            ),
        )

        assertEquals("test", stringPref.get())
        assertEquals(100, intPref.get())
        assertEquals(999L, longPref.get())
        assertEquals(3.14f, floatPref.get())
        assertEquals(true, boolPref.get())
        assertEquals(setOf("a", "b", "c"), stringSetPref.get())
    }

    @Test
    fun batchSet_handlesSerializedObjects() = runTest(testDispatcher) {
        data class TestObject(val id: Int, val name: String)

        val pref = preferenceDatastore.serialized(
            key = "batch_serialized",
            defaultValue = TestObject(0, ""),
            serializer = { "${it.id},${it.name}" },
            deserializer = {
                val parts = it.split(",", limit = 2)
                TestObject(parts[0].toInt(), parts[1])
            },
        )

        preferenceDatastore.batchSet(
            mapOf(pref to TestObject(42, "test")),
        )

        val result = pref.get()
        assertEquals(42, result.id)
        assertEquals("test", result.name)
    }

    @Test
    fun batchDelete_removesMultiplePreferences() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("delete_1", "default1")
        val pref2 = preferenceDatastore.int("delete_2", 0)
        val pref3 = preferenceDatastore.bool("delete_3", false)

        // Set values
        pref1.set("value1")
        pref2.set(100)
        pref3.set(true)

        // Verify they're set
        assertEquals("value1", pref1.get())
        assertEquals(100, pref2.get())
        assertEquals(true, pref3.get())

        // Batch delete
        preferenceDatastore.batchDelete(listOf(pref1, pref2, pref3))

        // Should return defaults
        assertEquals("default1", pref1.get())
        assertEquals(0, pref2.get())
        assertEquals(false, pref3.get())
    }

    @Test
    fun batchDelete_handlesEmptyList() = runTest(testDispatcher) {
        // Should not throw
        preferenceDatastore.batchDelete(emptyList())
    }

    @Test
    fun batchDelete_invalidatesCache() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("cache_invalidate_delete", "default")
        pref.set("value")

        // Populate cache
        assertEquals("value", pref.get())

        // Batch delete should invalidate cache
        preferenceDatastore.batchDelete(listOf(pref))

        // Should get default value (cache was invalidated)
        assertEquals("default", pref.get())
    }

    @Test
    fun batchOperations_performanceBenefit() = runTest(testDispatcher) {
        val preferences = (1..50).map {
            preferenceDatastore.int("perf_pref_$it", 0)
        }

        // Batch set performance test
        val batchSetStart = TimeSource.Monotonic.markNow()
        val updates: Map<Prefs<*>, Any?> = preferences.associate { it to 42 }
        preferenceDatastore.batchSet(updates)
        val batchSetDuration = batchSetStart.elapsedNow()

        // Verify all were set
        preferences.forEach { pref ->
            assertEquals(42, pref.get())
        }

        // Batch get performance test
        val batchGetStart = TimeSource.Monotonic.markNow()
        val results = preferenceDatastore.batchGet(preferences)
        val batchGetDuration = batchGetStart.elapsedNow()

        assertEquals(50, results.size)

        // Batch delete performance test
        val batchDeleteStart = TimeSource.Monotonic.markNow()
        preferenceDatastore.batchDelete(preferences)
        val batchDeleteDuration = batchDeleteStart.elapsedNow()

        // Batch operations should be reasonably fast
        // (These are loose bounds to avoid flaky tests)
        assert(batchSetDuration < 5.seconds) {
            "Batch set took too long: $batchSetDuration"
        }
        assert(batchGetDuration < 2.seconds) {
            "Batch get took too long: $batchGetDuration"
        }
        assert(batchDeleteDuration < 5.seconds) {
            "Batch delete took too long: $batchDeleteDuration"
        }
    }

    @Test
    fun batchOperations_mixedTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("mixed_string", "")
        val intPref = preferenceDatastore.int("mixed_int", 0)
        val boolPref = preferenceDatastore.bool("mixed_bool", false)

        // Batch set with mixed types
        preferenceDatastore.batchSet(
            mapOf(
                stringPref to "test",
                intPref to 999,
                boolPref to true,
            ),
        )

        // Batch get with mixed types
        val stringResults = preferenceDatastore.batchGet(listOf(stringPref))
        val intResults = preferenceDatastore.batchGet(listOf(intPref))
        val boolResults = preferenceDatastore.batchGet(listOf(boolPref))

        assertEquals("test", stringResults["mixed_string"])
        assertEquals(999, intResults["mixed_int"])
        assertEquals(true, boolResults["mixed_bool"])

        // Batch delete with mixed types
        preferenceDatastore.batchDelete(listOf(stringPref, intPref, boolPref))

        assertEquals("", stringPref.get())
        assertEquals(0, intPref.get())
        assertEquals(false, boolPref.get())
    }
}
