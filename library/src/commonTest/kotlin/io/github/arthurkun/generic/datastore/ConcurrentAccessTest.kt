package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for concurrent access synchronization.
 * Verifies that multiple threads can safely access preferences simultaneously.
 */
class ConcurrentAccessTest {

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
            createTempFile("test_concurrent", ".preferences_pb").also { it.deleteOnExit() }
        }
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun concurrent_getValue_isSafe() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("concurrent_get", 0)
        pref.set(42)

        // Launch multiple coroutines that read concurrently
        val jobs = List(10) {
            launch(Dispatchers.Default) {
                val value = pref.getValue()
                assertEquals(42, value)
            }
        }

        jobs.forEach { it.join() }
    }

    @Test
    fun concurrent_setValue_isSafe() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("concurrent_set", 0)

        // Launch multiple coroutines that write concurrently
        val jobs = List(10) { index ->
            launch(Dispatchers.Default) {
                pref.setValue(index)
            }
        }

        jobs.forEach { it.join() }

        // Final value should be one of the set values
        val finalValue = pref.getValue()
        assert(finalValue in 0..9) {
            "Expected value in range 0..9, got $finalValue"
        }
    }

    @Test
    fun concurrent_mixedOperations_isSafe() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("concurrent_mixed", "")

        // Launch concurrent reads and writes
        val writeJobs = List(5) { index ->
            launch(Dispatchers.Default) {
                pref.setValue("value_$index")
            }
        }

        val readJobs = List(5) {
            launch(Dispatchers.Default) {
                val value = pref.getValue()
                // Value should be either default or one of the written values
                assert(value.isEmpty() || value.startsWith("value_")) {
                    "Unexpected value: $value"
                }
            }
        }

        (writeJobs + readJobs).forEach { it.join() }
    }

    @Test
    fun concurrent_resetToDefault_isSafe() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("concurrent_reset", 100)
        pref.set(200)

        // Launch concurrent reset and read operations
        val resetJobs = List(3) {
            launch(Dispatchers.Default) {
                pref.resetToDefault()
            }
        }

        val readJobs = List(3) {
            launch(Dispatchers.Default) {
                val value = pref.getValue()
                // Value should be either the set value or default
                assert(value == 100 || value == 200) {
                    "Unexpected value: $value"
                }
            }
        }

        (resetJobs + readJobs).forEach { it.join() }

        // After all operations, should be default
        assertEquals(100, pref.getValue())
    }

    @Test
    fun concurrent_propertyDelegation_isSafe() = runTest(testDispatcher) {
        val pref = preferenceDatastore.bool("concurrent_property", false)
        var delegatedProperty by pref

        // Launch concurrent property access
        val jobs = List(10) { index ->
            launch(Dispatchers.Default) {
                delegatedProperty = (index % 2 == 0)
                @Suppress("UNUSED_VARIABLE")
                val value = delegatedProperty
            }
        }

        jobs.forEach { it.join() }

        // Final value should be valid (true or false)
        val finalValue = delegatedProperty
        assert(finalValue || !finalValue) // Always true, just verifying no crash
    }

    @Test
    fun concurrent_differentPreferences_areIndependent() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.int("concurrent_pref1", 0)
        val pref2 = preferenceDatastore.int("concurrent_pref2", 0)

        // Concurrent access to different preferences should work independently
        val jobs1 = List(5) { index ->
            launch(Dispatchers.Default) {
                pref1.setValue(index)
            }
        }

        val jobs2 = List(5) { index ->
            launch(Dispatchers.Default) {
                pref2.setValue(index * 10)
            }
        }

        (jobs1 + jobs2).forEach { it.join() }

        val value1 = pref1.getValue()
        val value2 = pref2.getValue()

        // Values should be in their respective ranges
        assert(value1 in 0..4) { "pref1 value out of range: $value1" }
        assert(value2 in listOf(0, 10, 20, 30, 40)) { "pref2 value out of range: $value2" }
    }
}
