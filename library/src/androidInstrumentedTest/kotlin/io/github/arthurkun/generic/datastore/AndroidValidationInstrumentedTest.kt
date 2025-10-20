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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Android instrumented tests for validation and error handling.
 */
@RunWith(AndroidJUnit4::class)
class AndroidValidationInstrumentedTest {

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
            produceFile = { testContext.preferencesDataStoreFile("test_validation_android") },
        )
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        val dataStoreFile =
            File(testContext.filesDir, "datastore/test_validation_android.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        testScope.cancel()
    }

    @Test
    fun preference_rejectsBlankKeys() {
        // Test all preference types reject blank keys
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.string("", "default")
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.int("", 0)
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.long("", 0L)
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.float("", 0f)
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.bool("", false)
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.stringSet("", emptySet())
        }
    }

    @Test
    fun preference_rejectsWhitespaceKeys() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.string("   ", "default")
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.int("\t", 0)
        }
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.long("\n", 0L)
        }
    }

    @Test
    fun preference_acceptsValidKeys() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("valid_key", "default")
        val intPref = preferenceDatastore.int("another_key", 0)

        assertEquals("valid_key", stringPref.key())
        assertEquals("another_key", intPref.key())
    }

    @Test
    fun serialized_preference_handlesSerializationErrors() = runTest(testDispatcher) {
        data class TestData(val value: Int)

        val pref = preferenceDatastore.serialized(
            key = "serialization_error_test",
            defaultValue = TestData(0),
            serializer = { throw RuntimeException("Serialization error") },
            deserializer = { TestData(it.toInt()) },
        )

        // Should not throw, should handle gracefully
        pref.set(TestData(42))

        // Should return default since serialization failed
        assertEquals(TestData(0), pref.get())
    }

    @Test
    fun serialized_preference_handlesDeserializationErrors() = runTest(testDispatcher) {
        data class TestData(val value: String)

        val pref = preferenceDatastore.serialized(
            key = "deserialization_error_test",
            defaultValue = TestData("default"),
            serializer = { it.value },
            deserializer = { throw RuntimeException("Deserialization error") },
        )

        // Should return default value on deserialization error
        val result = pref.get()
        assertEquals(TestData("default"), result)
    }

    @Test
    fun preference_handlesDataStoreErrors() = runTest(testDispatcher) {
        // Test that preferences handle errors gracefully
        val pref = preferenceDatastore.string("error_handling_test", "default")

        // Even if there's an issue, should return default
        val value = pref.get()
        assertEquals("default", value)
    }

    @Test
    fun mapped_preference_handlesConversionErrors() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("conversion_base", 100)
        val mappedPref = intPref.map(
            defaultValue = "ERROR",
            convert = { throw RuntimeException("Conversion failed") },
            reverse = { it.toInt() },
        )

        intPref.set(200)

        // Should return default on conversion error
        assertEquals("ERROR", mappedPref.get())
    }

    @Test
    fun mapped_preference_handlesReverseErrors() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("reverse_base", 50)
        val mappedPref = intPref.map(
            defaultValue = "DEFAULT",
            convert = { it.toString() },
            reverse = { throw RuntimeException("Reverse failed") },
        )

        mappedPref.set("100")

        // Should fallback to base default when reverse fails
        assertEquals(50, intPref.get())
    }
}
