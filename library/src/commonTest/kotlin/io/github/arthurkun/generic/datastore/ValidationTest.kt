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
import kotlin.test.assertFailsWith

private enum class ValidationTestEnum { A, B }

private enum class InvalidEnumTest { VALUE_A, VALUE_B }

/**
 * Tests for input validation and error handling in GenericPreferenceDatastore.
 */
class ValidationTest {

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
            // Use in-memory file for testing
            createTempFile("test_validation", ".preferences_pb").also { it.deleteOnExit() }
        }
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun string_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.string("", "default")
        }
    }

    @Test
    fun string_preference_rejectsWhitespaceKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.string("   ", "default")
        }
    }

    @Test
    fun int_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.int("", 0)
        }
    }

    @Test
    fun long_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.long("", 0L)
        }
    }

    @Test
    fun float_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.float("", 0f)
        }
    }

    @Test
    fun bool_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.bool("", false)
        }
    }

    @Test
    fun stringSet_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.stringSet("", emptySet())
        }
    }

    @Test
    fun serialized_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.serialized(
                key = "",
                defaultValue = "test",
                serializer = { it },
                deserializer = { it },
            )
        }
    }

    @Test
    fun enum_preference_rejectsBlankKey() {
        assertFailsWith<IllegalArgumentException> {
            preferenceDatastore.enum("", ValidationTestEnum.A)
        }
    }

    @Test
    fun string_preference_acceptsValidKey() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("valid_key", "default")
        assertEquals("valid_key", pref.key())
    }

    @Test
    fun serialized_preference_handlesSerializationError() = runTest(testDispatcher) {
        data class TestData(val value: String)

        val pref = preferenceDatastore.serialized(
            key = "test_serialization_error",
            defaultValue = TestData("default"),
            serializer = { throw RuntimeException("Serialization failed") },
            deserializer = { TestData(it) },
        )

        // Setting should fail silently without throwing
        pref.set(TestData("new"))

        // Should return default value since set failed
        val result = pref.get()
        assertEquals(TestData("default"), result)
    }

    @Test
    fun serialized_preference_handlesDeserializationError() = runTest(testDispatcher) {
        data class TestData(val value: String)

        val pref = preferenceDatastore.serialized(
            key = "test_deserialization_error",
            defaultValue = TestData("default"),
            serializer = { it.value },
            deserializer = { throw RuntimeException("Deserialization failed") },
        )

        // Should return default value on deserialization error
        val result = pref.get()
        assertEquals(TestData("default"), result)
    }

    @Test
    fun enum_preference_handlesInvalidEnumValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enum("test_enum_invalid", InvalidEnumTest.VALUE_A)

        // First set a valid value
        pref.set(InvalidEnumTest.VALUE_B)
        assertEquals(InvalidEnumTest.VALUE_B, pref.get())

        // Manually corrupt the stored value using a string preference
        val stringPref = preferenceDatastore.string("test_enum_invalid", "")
        stringPref.set("INVALID_VALUE")

        // Should return default value when encountering invalid enum
        assertEquals(InvalidEnumTest.VALUE_A, pref.get())
    }

    @Test
    fun mapped_preference_handlesConversionError() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("base_for_conversion_error", 10)
        val mappedPref = intPref.map(
            defaultValue = "DEFAULT",
            convert = { throw RuntimeException("Conversion error") },
            reverse = { it.toInt() },
        )

        intPref.set(20)

        // Should return default value on conversion error
        assertEquals("DEFAULT", mappedPref.get())
    }

    @Test
    fun mapped_preference_handlesReverseError() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("base_for_reverse_error", 10)
        val mappedPref = intPref.map(
            defaultValue = "DEFAULT",
            convert = { it.toString() },
            reverse = { throw RuntimeException("Reverse error") },
        )

        intPref.set(20)
        assertEquals("20", mappedPref.get())

        // Attempt to set via mapped preference with reverse error
        mappedPref.set("30")

        // Should fallback to default value of base preference
        assertEquals(10, intPref.get())
    }
}
