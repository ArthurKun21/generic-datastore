package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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

private const val TEST_DATASTORE_NAME = "test_datastore_blocking"

private enum class TestEnumBlocking { VALUE_A, VALUE_B }

private data class SerializableObjectBlocking(val id: Int, val name: String)


class DesktopDatastoreBlockingTest {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
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
            }
        )
        // Assuming GenericPreferenceDatastore takes a scope for its operations and for PrefsImpl
        preferenceDatastore = GenericPreferenceDatastore(dataStore, testScope)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        // TemporaryFolder will clean up the DataStore file
        // Cancel the scope to cancel any ongoing coroutines
        testScope.cancel()
    }


    // Tests for StringPreference
    @Test
    fun stringPreference_resetToDefault() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringReset", "defaultValueReset")
        stringPref.set("valueToReset")
        assertEquals(stringPref.get(), "valueToReset")

        stringPref.resetToDefault()
        assertEquals(stringPref.get(), "defaultValueReset")
    }

    // Tests for IntPreference
    @Test
    fun intPreference_resetToDefault() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testInt", 10)
        intPref.set(20)
        assertEquals(intPref.get(), 20)

        intPref.resetToDefault()
        assertEquals(intPref.get(), 10)
    }

    // Tests for LongPreference
    @Test
    fun longPreference_resetToDefault() = runTest(testDispatcher) {
        val longPref = preferenceDatastore.long("testLong", 100L)
        longPref.set(200L)
        assertEquals(longPref.get(), 200L)

        longPref.resetToDefault()
        assertEquals(longPref.get(), 100L)
    }

    // Tests for FloatPreference
    @Test
    fun floatPreference_resetToDefault() = runTest(testDispatcher) {
        val floatPref = preferenceDatastore.float("testFloat", 1.0f)
        floatPref.set(2.0f)
        assertEquals(floatPref.get(), 2.0f)

        floatPref.resetToDefault()
        assertEquals(floatPref.get(), 1.0f)
    }

    // Tests for BooleanPreference
    @Test
    fun booleanPreference_resetToDefault() = runTest(testDispatcher) {
        val boolPref = preferenceDatastore.bool("testBoolean", false)
        boolPref.set(true)
        assertEquals(boolPref.get(), true)

        boolPref.resetToDefault()
        assertEquals(boolPref.get(), false)
    }

    // Tests for StringSetPreference
    @Test
    fun stringSetPreference_resetToDefault() = runTest(testDispatcher) {
        val stringSetPref = preferenceDatastore.stringSet("testStringSet", setOf("a", "b"))
        stringSetPref.set(setOf("c", "d", "e"))
        assertEquals(stringSetPref.get(), setOf("c", "d", "e"))

        stringSetPref.resetToDefault()
        assertEquals(stringSetPref.get(), setOf("a", "b"))
    }

    // Tests for EnumPreference
    @Test
    fun enumPreference_setAndGetValue() = runTest(testDispatcher) {
        val enumPref = preferenceDatastore.enum("testEnum", TestEnumBlocking.VALUE_A)
        enumPref.set(TestEnumBlocking.VALUE_B)
        assertEquals(enumPref.get(), TestEnumBlocking.VALUE_B)

        enumPref.resetToDefault()
        assertEquals(enumPref.get(), TestEnumBlocking.VALUE_A)
    }

    // Tests for Serialized (ObjectPrimitive)
    @Test
    fun serializedPreference_resetToDefault() = runTest(testDispatcher) {
        val defaultObj = SerializableObjectBlocking(5, "DefaultReset")
        val objToReset = SerializableObjectBlocking(6, "ToReset")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedReset",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObjectBlocking(parts[0].toInt(), parts[1])
            }
        )
        serializedPref.set(objToReset)
        assertEquals(serializedPref.get(), objToReset)

        serializedPref.resetToDefault()
        assertEquals(serializedPref.get(), defaultObj)
    }

    // Tests for MappedPreference
    @Test
    fun mappedPreference_resetToDefault() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapReset", 75)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultReset",
            convert = { "ResetMapped_$it" },
            reverse = { it.removePrefix("ResetMapped_").toInt() }
        )
        mappedPref.set("ResetMapped_750")
        assertEquals(mappedPref.get(), "ResetMapped_750")
        assertEquals(intPref.get(), 750)

        mappedPref.resetToDefault() // This should reset the underlying intPref to its default
        assertEquals(
            mappedPref.get(),
            "ResetMapped_75"
        ) // Mapped pref would return the converted default
        assertEquals(intPref.get(), 75) // Base pref should be reset to its default
    }

}
