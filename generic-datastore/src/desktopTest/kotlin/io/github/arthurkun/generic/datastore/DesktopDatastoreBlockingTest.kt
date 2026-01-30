package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.map
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.enum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

private const val TEST_DATASTORE_BLOCKING_NAME = "test_datastore_blocking"

private enum class TestEnumBlocking { VALUE_A, VALUE_B }

private data class SerializableObjectBlocking(val id: Int, val name: String)

class DesktopDatastoreBlockingTest {

    @TempDir
    lateinit var tempFolder: File

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferencesDatastore
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                File(tempFolder, "${TEST_DATASTORE_BLOCKING_NAME}.preferences_pb")
            },
        )
        // Assuming GenericPreferencesDatastore uses the provided DataStore for its operations
        preferenceDatastore = GenericPreferencesDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Tests for StringPreference
    @Test
    fun stringPreference_resetToDefault() {
        val stringPref = preferenceDatastore.string("testString", "defaultValueReset")
        stringPref.setBlocking("valueToReset")
        assertEquals(stringPref.getBlocking(), "valueToReset")

        stringPref.resetToDefault()
        assertEquals(stringPref.getBlocking(), "defaultValueReset")
    }

    // Tests for IntPreference
    @Test
    fun intPreference_resetToDefault() {
        val intPref = preferenceDatastore.int("testInt", 10)
        intPref.setBlocking(20)
        assertEquals(intPref.getBlocking(), 20)

        intPref.resetToDefault()
        assertEquals(intPref.getBlocking(), 10)
    }

    // Tests for LongPreference
    @Test
    fun longPreference_resetToDefault() {
        val longPref = preferenceDatastore.long("testLong", 100L)
        longPref.setBlocking(200L)
        assertEquals(longPref.getBlocking(), 200L)

        longPref.resetToDefault()
        assertEquals(longPref.getBlocking(), 100L)
    }

    // Tests for FloatPreference
    @Test
    fun floatPreference_resetToDefault() {
        val floatPref = preferenceDatastore.float("testFloat", 1.0f)
        floatPref.setBlocking(2.0f)
        assertEquals(floatPref.getBlocking(), 2.0f)

        floatPref.resetToDefault()
        assertEquals(floatPref.getBlocking(), 1.0f)
    }

    // Tests for BooleanPreference
    @Test
    fun booleanPreference_resetToDefault() {
        val boolPref = preferenceDatastore.bool("testBoolean", false)
        boolPref.setBlocking(true)
        assertEquals(boolPref.getBlocking(), true)

        boolPref.resetToDefault()
        assertEquals(boolPref.getBlocking(), false)
    }

    // Tests for StringSetPreference
    @Test
    fun stringSetPreference_resetToDefault() {
        val stringSetPref = preferenceDatastore.stringSet("testStringSet", setOf("a", "b"))
        stringSetPref.setBlocking(setOf("c", "d", "e"))
        assertEquals(stringSetPref.getBlocking(), setOf("c", "d", "e"))

        stringSetPref.resetToDefault()
        assertEquals(stringSetPref.getBlocking(), setOf("a", "b"))
    }

    // Tests for EnumPreference
    @Test
    fun enumPreference_setAndGetValue() {
        val enumPref = preferenceDatastore.enum("testEnum", TestEnumBlocking.VALUE_A)
        enumPref.setBlocking(TestEnumBlocking.VALUE_B)
        assertEquals(enumPref.getBlocking(), TestEnumBlocking.VALUE_B)

        enumPref.resetToDefault()
        assertEquals(enumPref.getBlocking(), TestEnumBlocking.VALUE_A)
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
            },
        )
        serializedPref.setBlocking(objToReset)
        assertEquals(serializedPref.getBlocking(), objToReset)

        serializedPref.resetToDefault()
        assertEquals(serializedPref.getBlocking(), defaultObj)
    }

    // Tests for MappedPreference
    @Test
    fun mappedPreference_resetToDefault() {
        val intPref = preferenceDatastore.int("baseForMapReset", 75)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultReset",
            convert = { "ResetMapped_$it" },
            reverse = { it.removePrefix("ResetMapped_").toInt() },
        )
        mappedPref.setBlocking("ResetMapped_750")
        assertEquals(mappedPref.getBlocking(), "ResetMapped_750")
        assertEquals(intPref.getBlocking(), 750)

        mappedPref.resetToDefault() // This should reset the underlying intPref to its default
        assertEquals(
            mappedPref.getBlocking(),
            "ResetMapped_75",
        ) // Mapped pref would return the converted default
        assertEquals(intPref.getBlocking(), 75) // Base pref should be reset to its default
    }

    // Test for StringPreference delegation
    @Test
    fun stringPreference_delegation() {
        val stringPref =
            preferenceDatastore.string("testStringDelegateDesktop", "defaultDelegateValueDesktop")
        var delegatedValue: String by stringPref

        // Set value via delegation
        delegatedValue = "newDelegateValueDesktop"
        assertEquals("newDelegateValueDesktop", delegatedValue)
        assertEquals("newDelegateValueDesktop", stringPref.getBlocking())

        // Reset to default
        stringPref.resetToDefault()
        assertEquals("defaultDelegateValueDesktop", delegatedValue)
        assertEquals("defaultDelegateValueDesktop", stringPref.getBlocking())
    }

    // Test for IntPreference delegation
    @Test
    fun intPreference_delegation() {
        val intPref = preferenceDatastore.int("testIntDelegateDesktop", 789)
        var delegatedValue: Int by intPref

        // Set value via delegation
        delegatedValue = 101
        assertEquals(101, delegatedValue)
        assertEquals(101, intPref.getBlocking())

        // Reset to default
        intPref.resetToDefault()
        assertEquals(789, delegatedValue)
        assertEquals(789, intPref.getBlocking())
    }

    // Test for LongPreference delegation
    @Test
    fun longPreference_delegation() {
        val longPref = preferenceDatastore.long("testLongDelegateDesktop", 789L)
        var delegatedValue: Long by longPref

        // Set value via delegation
        delegatedValue = 101L
        assertEquals(101L, delegatedValue)
        assertEquals(101L, longPref.getBlocking())

        // Reset to default
        longPref.resetToDefault()
        assertEquals(789L, delegatedValue)
        assertEquals(789L, longPref.getBlocking())
    }

    // Test for FloatPreference delegation
    @Test
    fun floatPreference_delegation() {
        val floatPref = preferenceDatastore.float("testFloatDelegateDesktop", 7.89f)
        var delegatedValue: Float by floatPref

        // Set value via delegation
        delegatedValue = 1.01f
        assertEquals(1.01f, delegatedValue)
        assertEquals(1.01f, floatPref.getBlocking())

        // Reset to default
        floatPref.resetToDefault()
        assertEquals(7.89f, delegatedValue)
        assertEquals(7.89f, floatPref.getBlocking())
    }

    // Test for BooleanPreference delegation
    @Test
    fun booleanPreference_delegation() {
        val boolPref = preferenceDatastore.bool("testBooleanDelegateDesktop", true)
        var delegatedValue: Boolean by boolPref

        // Set value via delegation
        delegatedValue = false
        assertEquals(false, delegatedValue)
        assertEquals(false, boolPref.getBlocking())

        // Reset to default
        boolPref.resetToDefault()
        assertEquals(true, delegatedValue)
        assertEquals(true, boolPref.getBlocking())
    }

    // Test for StringSetPreference delegation
    @Test
    fun stringSetPreference_delegation() {
        val stringSetPref =
            preferenceDatastore.stringSet("testStringSetDelegateDesktop", setOf("x", "y"))
        var delegatedValue: Set<String> by stringSetPref

        // Set value via delegation
        delegatedValue = setOf("z", "w")
        assertEquals(setOf("z", "w"), delegatedValue)
        assertEquals(setOf("z", "w"), stringSetPref.getBlocking())

        // Reset to default
        stringSetPref.resetToDefault()
        assertEquals(setOf("x", "y"), delegatedValue)
        assertEquals(setOf("x", "y"), stringSetPref.getBlocking())
    }

    // Test for EnumPreference delegation
    @Test
    fun enumPreference_delegation() {
        val enumPref = preferenceDatastore.enum("testEnumDelegateDesktop", TestEnumBlocking.VALUE_A)
        var delegatedValue: TestEnumBlocking by enumPref

        // Set value via delegation
        delegatedValue = TestEnumBlocking.VALUE_B
        assertEquals(TestEnumBlocking.VALUE_B, delegatedValue)
        assertEquals(TestEnumBlocking.VALUE_B, enumPref.getBlocking())

        // Reset to default
        enumPref.resetToDefault()
        assertEquals(TestEnumBlocking.VALUE_A, delegatedValue)
        assertEquals(TestEnumBlocking.VALUE_A, enumPref.getBlocking())
    }

    // Test for SerializedPreference delegation
    @Test
    fun serializedPreference_delegation() {
        val defaultObj = SerializableObjectBlocking(3, "DefaultDelegateDesktop")
        val newObj = SerializableObjectBlocking(4, "NewDelegateDesktop")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedDelegateDesktop",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObjectBlocking(parts[0].toInt(), parts[1])
            },
        )
        var delegatedValue: SerializableObjectBlocking by serializedPref

        // Set value via delegation
        delegatedValue = newObj
        assertEquals(newObj, delegatedValue)
        assertEquals(newObj, serializedPref.getBlocking())

        // Reset to default
        serializedPref.resetToDefault()
        assertEquals(defaultObj, delegatedValue)
        assertEquals(defaultObj, serializedPref.getBlocking())
    }

    // Test for MappedPreference delegation
    @Test
    fun mappedPreference_delegation() {
        val intPref = preferenceDatastore.int("baseForMapDelegateDesktop", 300)
        val mappedPref = intPref.map(
            defaultValue = "MappedDelegateDefaultDesktop",
            convert = { "DelegateMappedDesktop_$it" },
            reverse = { it.removePrefix("DelegateMappedDesktop_").toInt() },
        )
        var delegatedValue: String by mappedPref

        // Set value via delegation
        delegatedValue = "DelegateMappedDesktop_400"
        assertEquals("DelegateMappedDesktop_400", delegatedValue)
        assertEquals("DelegateMappedDesktop_400", mappedPref.getBlocking())
        assertEquals(400, intPref.getBlocking()) // Check underlying preference

        // Reset to default
        mappedPref.resetToDefault() // This should reset the underlying intPref to its default
        assertEquals(
            "DelegateMappedDesktop_300",
            delegatedValue,
        ) // Mapped pref would return the converted default
        assertEquals("DelegateMappedDesktop_300", mappedPref.getBlocking())
        assertEquals(300, intPref.getBlocking()) // Base pref should be reset to its default
    }
}
