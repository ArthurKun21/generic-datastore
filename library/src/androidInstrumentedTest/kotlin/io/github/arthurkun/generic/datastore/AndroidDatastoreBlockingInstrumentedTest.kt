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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals

private const val TEST_DATASTORE_NAME = "test_datastore_blocking"

private enum class TestEnumBlocking { VALUE_A, VALUE_B }

private data class SerializableObjectBlocking(val id: Int, val name: String)

@RunWith(AndroidJUnit4::class)
class AndroidDatastoreBlockingInstrumentedTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
    private lateinit var testContext: Context
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE_NAME) }
        )
        // Assuming GenericPreferenceDatastore takes a scope for its operations and for PrefsImpl
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Clean up DataStore file
        val dataStoreFile =
            File(testContext.filesDir, "datastore/${TEST_DATASTORE_NAME}.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        // Cancel the scope to cancel any ongoing coroutines
        testScope.cancel()
    }

    // Tests for StringPreference
    @Test
    fun stringPreference_resetToDefault() {
        val stringPref = preferenceDatastore.string("testStringReset", "defaultValueReset")
        stringPref.setValue("valueToReset")
        assertEquals(stringPref.getValue(), "valueToReset")

        stringPref.resetToDefault()
        assertEquals(stringPref.getValue(), "defaultValueReset")
    }

    // Tests for IntPreference
    @Test
    fun intPreference_resetToDefault() {
        val intPref = preferenceDatastore.int("testInt", 10)
        intPref.setValue(20)
        assertEquals(intPref.getValue(), 20)

        intPref.resetToDefault()
        assertEquals(intPref.getValue(), 10)
    }

    // Tests for LongPreference
    @Test
    fun longPreference_resetToDefault() {
        val longPref = preferenceDatastore.long("testLong", 100L)
        longPref.setValue(200L)
        assertEquals(longPref.getValue(), 200L)

        longPref.resetToDefault()
        assertEquals(longPref.getValue(), 100L)
    }

    // Tests for FloatPreference
    @Test
    fun floatPreference_resetToDefault() {
        val floatPref = preferenceDatastore.float("testFloat", 1.0f)
        floatPref.setValue(2.0f)
        assertEquals(floatPref.getValue(), 2.0f)

        floatPref.resetToDefault()
        assertEquals(floatPref.getValue(), 1.0f)
    }

    // Tests for BooleanPreference
    @Test
    fun booleanPreference_resetToDefault() {
        val boolPref = preferenceDatastore.bool("testBoolean", false)
        boolPref.setValue(true)
        assertEquals(boolPref.getValue(), true)

        boolPref.resetToDefault()
        assertEquals(boolPref.getValue(), false)
    }

    // Tests for StringSetPreference
    @Test
    fun stringSetPreference_resetToDefault() {
        val stringSetPref = preferenceDatastore.stringSet("testStringSet", setOf("a", "b"))
        stringSetPref.setValue(setOf("c", "d", "e"))
        assertEquals(stringSetPref.getValue(), setOf("c", "d", "e"))

        stringSetPref.resetToDefault()
        assertEquals(stringSetPref.getValue(), setOf("a", "b"))
    }

    // Tests for EnumPreference
    @Test
    fun enumPreference_setAndGetValue() {
        val enumPref = preferenceDatastore.enum("testEnum", TestEnumBlocking.VALUE_A)
        enumPref.setValue(TestEnumBlocking.VALUE_B)
        assertEquals(enumPref.getValue(), TestEnumBlocking.VALUE_B)

        enumPref.resetToDefault()
        assertEquals(enumPref.getValue(), TestEnumBlocking.VALUE_A)
    }

    // Tests for Serialized (ObjectPrimitive)
    @Test
    fun serializedPreference_resetToDefault() {
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
        serializedPref.setValue(objToReset)
        assertEquals(serializedPref.getValue(), objToReset)

        serializedPref.resetToDefault()
        assertEquals(serializedPref.getValue(), defaultObj)
    }

    // Tests for MappedPreference
    @Test
    fun mappedPreference_resetToDefault() {
        val intPref = preferenceDatastore.int("baseForMapReset", 75)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultReset",
            convert = { "ResetMapped_$it" },
            reverse = { it.removePrefix("ResetMapped_").toInt() }
        )
        mappedPref.setValue("ResetMapped_750")
        assertEquals(mappedPref.getValue(), "ResetMapped_750")
        assertEquals(intPref.getValue(), 750)

        mappedPref.resetToDefault() // This should reset the underlying intPref to its default
        assertEquals(
            mappedPref.getValue(),
            "ResetMapped_75"
        ) // Mapped pref would return the converted default
        assertEquals(intPref.getValue(), 75) // Base pref should be reset to its default
    }
}
