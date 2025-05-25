package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals

private const val TEST_DATASTORE_BLOCKING_NAME = "test_datastore_blocking"

private enum class TestEnumBlocking { VALUE_A, VALUE_B }

private data class SerializableObjectBlocking(val id: Int, val name: String)

@RunWith(AndroidJUnit4::class)
class AndroidDatastoreBlockingInstrumentedTest {

    companion object {
        private lateinit var dataStore: DataStore<Preferences>
        private lateinit var preferenceDatastore: GenericPreferenceDatastore
        private lateinit var testContext: Context
        private val testDispatcher = UnconfinedTestDispatcher()

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            Dispatchers.setMain(testDispatcher)
            testContext = ApplicationProvider.getApplicationContext()
            dataStore = PreferenceDataStoreFactory.create(
                produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE_BLOCKING_NAME) }
            )
            preferenceDatastore = GenericPreferenceDatastore(dataStore)
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            Dispatchers.resetMain()
            // Clean up DataStore file
            val dataStoreFile =
                File(testContext.filesDir, "datastore/${TEST_DATASTORE_BLOCKING_NAME}.preferences_pb")
            if (dataStoreFile.exists()) {
                dataStoreFile.delete()
            }
        }
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

    // Test for StringPreference delegation
    @Test
    fun stringPreference_delegation() {
        val stringPref = preferenceDatastore.string("testStringDelegate", "defaultDelegateValue")
        var delegatedValue: String by stringPref

        // Set value via delegation
        delegatedValue = "newDelegateValue"
        assertEquals("newDelegateValue", delegatedValue)
        assertEquals("newDelegateValue", stringPref.getValue())

        // Reset to default
        stringPref.resetToDefault()
        assertEquals("defaultDelegateValue", delegatedValue)
        assertEquals("defaultDelegateValue", stringPref.getValue())
    }

    // Test for IntPreference delegation
    @Test
    fun intPreference_delegation() {
        val intPref = preferenceDatastore.int("testIntDelegate", 123)
        var delegatedValue: Int by intPref

        // Set value via delegation
        delegatedValue = 456
        assertEquals(456, delegatedValue)
        assertEquals(456, intPref.getValue())

        // Reset to default
        intPref.resetToDefault()
        assertEquals(123, delegatedValue)
        assertEquals(123, intPref.getValue())
    }

    // Test for LongPreference delegation
    @Test
    fun longPreference_delegation() {
        val longPref = preferenceDatastore.long("testLongDelegate", 123L)
        var delegatedValue: Long by longPref

        // Set value via delegation
        delegatedValue = 456L
        assertEquals(456L, delegatedValue)
        assertEquals(456L, longPref.getValue())

        // Reset to default
        longPref.resetToDefault()
        assertEquals(123L, delegatedValue)
        assertEquals(123L, longPref.getValue())
    }

    // Test for FloatPreference delegation
    @Test
    fun floatPreference_delegation() {
        val floatPref = preferenceDatastore.float("testFloatDelegate", 1.23f)
        var delegatedValue: Float by floatPref

        // Set value via delegation
        delegatedValue = 4.56f
        assertEquals(4.56f, delegatedValue)
        assertEquals(4.56f, floatPref.getValue())

        // Reset to default
        floatPref.resetToDefault()
        assertEquals(1.23f, delegatedValue)
        assertEquals(1.23f, floatPref.getValue())
    }

    // Test for BooleanPreference delegation
    @Test
    fun booleanPreference_delegation() {
        val boolPref = preferenceDatastore.bool("testBooleanDelegate", false)
        var delegatedValue: Boolean by boolPref

        // Set value via delegation
        delegatedValue = true
        assertEquals(true, delegatedValue)
        assertEquals(true, boolPref.getValue())

        // Reset to default
        boolPref.resetToDefault()
        assertEquals(false, delegatedValue)
        assertEquals(false, boolPref.getValue())
    }

    // Test for StringSetPreference delegation
    @Test
    fun stringSetPreference_delegation() {
        val stringSetPref = preferenceDatastore.stringSet("testStringSetDelegate", setOf("a", "b"))
        var delegatedValue: Set<String> by stringSetPref

        // Set value via delegation
        delegatedValue = setOf("c", "d")
        assertEquals(setOf("c", "d"), delegatedValue)
        assertEquals(setOf("c", "d"), stringSetPref.getValue())

        // Reset to default
        stringSetPref.resetToDefault()
        assertEquals(setOf("a", "b"), delegatedValue)
        assertEquals(setOf("a", "b"), stringSetPref.getValue())
    }

    // Test for EnumPreference delegation
    @Test
    fun enumPreference_delegation() {
        val enumPref = preferenceDatastore.enum("testEnumDelegate", TestEnumBlocking.VALUE_A)
        var delegatedValue: TestEnumBlocking by enumPref

        // Set value via delegation
        delegatedValue = TestEnumBlocking.VALUE_B
        assertEquals(TestEnumBlocking.VALUE_B, delegatedValue)
        assertEquals(TestEnumBlocking.VALUE_B, enumPref.getValue())

        // Reset to default
        enumPref.resetToDefault()
        assertEquals(TestEnumBlocking.VALUE_A, delegatedValue)
        assertEquals(TestEnumBlocking.VALUE_A, enumPref.getValue())
    }

    // Test for SerializedPreference delegation
    @Test
    fun serializedPreference_delegation() {
        val defaultObj = SerializableObjectBlocking(1, "DefaultDelegate")
        val newObj = SerializableObjectBlocking(2, "NewDelegate")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedDelegate",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObjectBlocking(parts[0].toInt(), parts[1])
            }
        )
        var delegatedValue: SerializableObjectBlocking by serializedPref

        // Set value via delegation
        delegatedValue = newObj
        assertEquals(newObj, delegatedValue)
        assertEquals(newObj, serializedPref.getValue())

        // Reset to default
        serializedPref.resetToDefault()
        assertEquals(defaultObj, delegatedValue)
        assertEquals(defaultObj, serializedPref.getValue())
    }

    // Test for MappedPreference delegation
    @Test
    fun mappedPreference_delegation() {
        val intPref = preferenceDatastore.int("baseForMapDelegate", 100)
        val mappedPref = intPref.map(
            defaultValue = "MappedDelegateDefault",
            convert = { "DelegateMapped_$it" },
            reverse = { it.removePrefix("DelegateMapped_").toInt() }
        )
        var delegatedValue: String by mappedPref

        // Set value via delegation
        delegatedValue = "DelegateMapped_200"
        assertEquals("DelegateMapped_200", delegatedValue)
        assertEquals("DelegateMapped_200", mappedPref.getValue())
        assertEquals(200, intPref.getValue()) // Check underlying preference

        // Reset to default
        mappedPref.resetToDefault() // This should reset the underlying intPref to its default
        assertEquals(
            "DelegateMapped_100",
            delegatedValue
        ) // Mapped pref would return the converted default
        assertEquals("DelegateMapped_100", mappedPref.getValue())
        assertEquals(100, intPref.getValue()) // Base pref should be reset to its default
    }
}
