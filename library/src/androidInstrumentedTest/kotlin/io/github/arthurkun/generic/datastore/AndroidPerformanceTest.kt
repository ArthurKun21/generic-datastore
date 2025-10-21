package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

/**
 * Performance tests for DataStore operations.
 * These tests verify that operations complete in reasonable time.
 */
@RunWith(AndroidJUnit4::class)
class AndroidPerformanceTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
    private lateinit var testContext: Context
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile("test_performance") },
        )
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        val dataStoreFile = File(testContext.filesDir, "datastore/test_performance.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        testScope.cancel()
    }

    @Test
    fun performance_multipleWrites() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("perf_write_test", 0)

        val timeMillis = measureTimeMillis {
            repeat(100) { i ->
                pref.set(i)
            }
        }

        // Should complete 100 writes in reasonable time (< 5 seconds)
        assertTrue(
            timeMillis < 5000,
            "100 writes took ${timeMillis}ms, expected < 5000ms",
        )
        println("Performance: 100 writes completed in ${timeMillis}ms")
    }

    @Test
    fun performance_multipleReads() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("perf_read_test", 42)
        pref.set(100) // Set initial value

        val timeMillis = measureTimeMillis {
            repeat(100) {
                pref.get()
            }
        }

        // Should complete 100 reads in reasonable time (< 2 seconds)
        assertTrue(
            timeMillis < 2000,
            "100 reads took ${timeMillis}ms, expected < 2000ms",
        )
        println("Performance: 100 reads completed in ${timeMillis}ms")
    }

    @Test
    fun performance_mixedOperations() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("perf_string", "")
        val intPref = preferenceDatastore.int("perf_int", 0)
        val boolPref = preferenceDatastore.bool("perf_bool", false)

        val timeMillis = measureTimeMillis {
            repeat(50) { i ->
                stringPref.set("value_$i")
                intPref.set(i)
                boolPref.set(i % 2 == 0)

                stringPref.get()
                intPref.get()
                boolPref.get()
            }
        }

        // Should complete 300 operations (50 * 6) in reasonable time (< 5 seconds)
        assertTrue(
            timeMillis < 5000,
            "300 mixed operations took ${timeMillis}ms, expected < 5000ms",
        )
        println("Performance: 300 mixed operations completed in ${timeMillis}ms")
    }

    @Test
    fun performance_exportOperation() = runTest(testDispatcher) {
        // Setup test data
        repeat(50) { i ->
            preferenceDatastore.string("key_$i", "").set("value_$i")
        }

        val timeMillis = measureTimeMillis {
            repeat(10) {
                preferenceDatastore.export()
            }
        }

        // Should complete 10 exports in reasonable time (< 2 seconds)
        assertTrue(
            timeMillis < 2000,
            "10 exports took ${timeMillis}ms, expected < 2000ms",
        )
        println("Performance: 10 exports of 50 preferences completed in ${timeMillis}ms")
    }

    @Test
    fun performance_importOperation() = runTest(testDispatcher) {
        val data = (0 until 50).associate { i ->
            "import_key_$i" to "import_value_$i"
        }

        val timeMillis = measureTimeMillis {
            repeat(10) {
                preferenceDatastore.import(data)
            }
        }

        // Should complete 10 imports in reasonable time (< 3 seconds)
        assertTrue(
            timeMillis < 3000,
            "10 imports took ${timeMillis}ms, expected < 3000ms",
        )
        println("Performance: 10 imports of 50 preferences completed in ${timeMillis}ms")
    }

    @Test
    fun performance_mappedPreference() = runTest(testDispatcher) {
        val basePref = preferenceDatastore.int("perf_mapped_base", 0)
        val mappedPref = basePref.map(
            defaultValue = "0",
            convert = { it.toString() },
            reverse = { it.toInt() },
        )

        val timeMillis = measureTimeMillis {
            repeat(100) { i ->
                mappedPref.set(i.toString())
                mappedPref.get()
            }
        }

        // Should complete 200 mapped operations in reasonable time (< 5 seconds)
        assertTrue(
            timeMillis < 5000,
            "200 mapped operations took ${timeMillis}ms, expected < 5000ms",
        )
        println("Performance: 200 mapped preference operations completed in ${timeMillis}ms")
    }

    @Test
    fun performance_serializedPreference() = runTest(testDispatcher) {
        data class TestObject(val id: Int, val name: String)

        val pref = preferenceDatastore.serialized(
            key = "perf_serialized",
            defaultValue = TestObject(0, "default"),
            serializer = { "${it.id},${it.name}" },
            deserializer = {
                val parts = it.split(",", limit = 2)
                TestObject(parts[0].toInt(), parts[1])
            },
        )

        val timeMillis = measureTimeMillis {
            repeat(100) { i ->
                pref.set(TestObject(i, "name_$i"))
                pref.get()
            }
        }

        // Should complete 200 serialized operations in reasonable time (< 5 seconds)
        assertTrue(
            timeMillis < 5000,
            "200 serialized operations took ${timeMillis}ms, expected < 5000ms",
        )
        println("Performance: 200 serialized preference operations completed in ${timeMillis}ms")
    }

    @Test
    fun performance_concurrentReads() = runTest(testDispatcher) {
        val prefs = (0 until 10).map { i ->
            preferenceDatastore.int("concurrent_$i", i)
        }

        // Set initial values
        prefs.forEachIndexed { i, pref ->
            pref.set(i * 10)
        }

        val timeMillis = measureTimeMillis {
            repeat(50) {
                // Read all preferences
                prefs.forEach { it.get() }
            }
        }

        // Should complete 500 reads (50 * 10) in reasonable time (< 2 seconds)
        assertTrue(
            timeMillis < 2000,
            "500 concurrent reads took ${timeMillis}ms, expected < 2000ms",
        )
        println("Performance: 500 concurrent reads completed in ${timeMillis}ms")
    }
}
