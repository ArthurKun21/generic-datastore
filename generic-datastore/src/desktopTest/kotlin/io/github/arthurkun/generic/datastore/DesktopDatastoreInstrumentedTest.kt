package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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

private const val TEST_DATASTORE_NAME = "test_datastore"

private enum class TestEnum { VALUE_A, VALUE_B, VALUE_C }

private data class SerializableObject(val id: Int, val name: String)

class DesktopDatastoreInstrumentedTest {

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
            },
        )
        // Assuming GenericPreferenceDatastore takes a scope for its operations and for PrefsImpl
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        // TemporaryFolder will clean up the DataStore file
        // Cancel the scope to cancel any ongoing coroutines
        testScope.cancel()
    }

    @Test
    fun stringPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testString", "defaultValue")
        assertEquals(stringPref.get(), "defaultValue")
    }

    @Test
    fun stringPreference_setAndGetValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testString", "defaultValue")
        stringPref.set("newValue")
        assertEquals(stringPref.get(), "newValue")
    }

    @Test
    fun stringPreference_observeDefaultValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringFlow", "defaultFlowValue")
        val value = stringPref.asFlow().first()
        assertEquals(value, "defaultFlowValue")
    }

    @Test
    fun stringPreference_observeSetValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringFlowSet", "defaultFlowSet")
        stringPref.set("newFlowValue")
        val value = stringPref.asFlow().first()

        assertEquals(value, "newFlowValue")
    }

    @Test
    fun stringPreference_getAndSetValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringGetAndSet", "initialValue")
        stringPref.getAndSet { currentValue -> "$currentValue-Updated" }
        assertEquals(stringPref.get(), "initialValue-Updated")
    }

    @Test
    fun stringPreference_deleteValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringDelete", "defaultValue")
        stringPref.set("valueToDelete")
        assertEquals(stringPref.get(), "valueToDelete") // Verify it's set

        stringPref.delete()
        assertEquals(stringPref.get(), "defaultValue") // Should revert to default
    }

    // Tests for IntPreference
    @Test
    fun intPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testInt", 10)
        assertEquals(intPref.get(), 10)
    }

    @Test
    fun intPreference_setAndGetValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testInt", 10)
        intPref.set(20)
        assertEquals(intPref.get(), 20)
    }

    @Test
    fun intPreference_observeSetValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testIntFlowSet", 30)
        intPref.set(40)
        val value = intPref.asFlow().first()
        assertEquals(value, 40)
    }

    // Tests for LongPreference
    @Test
    fun longPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val longPref = preferenceDatastore.long("testLong", 100L)
        assertEquals(longPref.get(), 100L)
    }

    @Test
    fun longPreference_setAndGetValue() = runTest(testDispatcher) {
        val longPref = preferenceDatastore.long("testLong", 100L)
        longPref.set(200L)
        assertEquals(longPref.get(), 200L)
    }

    // Tests for FloatPreference
    @Test
    fun floatPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val floatPref = preferenceDatastore.float("testFloat", 1.0f)
        assertEquals(floatPref.get(), 1.0f)
    }

    @Test
    fun floatPreference_setAndGetValue() = runTest(testDispatcher) {
        val floatPref = preferenceDatastore.float("testFloat", 1.0f)
        floatPref.set(2.5f)
        assertEquals(floatPref.get(), 2.5f)
    }

    // Tests for BooleanPreference
    @Test
    fun booleanPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val boolPref = preferenceDatastore.bool("testBoolean", false)
        assertEquals(boolPref.get(), false)
    }

    @Test
    fun booleanPreference_setAndGetValue() = runTest(testDispatcher) {
        val boolPref = preferenceDatastore.bool("testBoolean", false)
        boolPref.set(true)
        assertEquals(boolPref.get(), true)
    }

    // Tests for StringSetPreference
    @Test
    fun stringSetPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val stringSetPref = preferenceDatastore.stringSet("testStringSet", setOf("a", "b"))
        assertEquals(stringSetPref.get(), setOf("a", "b"))
    }

    @Test
    fun stringSetPreference_setAndGetValue() = runTest(testDispatcher) {
        val stringSetPref = preferenceDatastore.stringSet("testStringSet", setOf("a", "b"))
        stringSetPref.set(setOf("c", "d", "e"))
        assertEquals(stringSetPref.get(), setOf("c", "d", "e"))
    }

    // Tests for EnumPreference
    @Test
    fun enumPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val enumPref = preferenceDatastore.enum("testEnum", TestEnum.VALUE_A)
        assertEquals(enumPref.get(), TestEnum.VALUE_A)
    }

    @Test
    fun enumPreference_setAndGetValue() = runTest(testDispatcher) {
        val enumPref = preferenceDatastore.enum("testEnum", TestEnum.VALUE_A)
        enumPref.set(TestEnum.VALUE_B)
        assertEquals(enumPref.get(), TestEnum.VALUE_B)
    }

    @Test
    fun enumPreference_observeSetValue() = runTest(testDispatcher) {
        val enumPref = preferenceDatastore.enum("testEnumFlowSet", TestEnum.VALUE_C)
        enumPref.set(TestEnum.VALUE_A)
        val value = enumPref.asFlow().first()
        assertEquals(value, TestEnum.VALUE_A)
    }

    @Test
    fun enumPreference_handleUnknownValueGracefully() = runTest(testDispatcher) {
        // First, set a valid enum value
        val enumPref = preferenceDatastore.enum("testEnumUnknown", TestEnum.VALUE_A)
        enumPref.set(TestEnum.VALUE_B)
        assertEquals(enumPref.get(), TestEnum.VALUE_B)

        // Manually edit the datastore to put an invalid enum string
        val stringKey = androidx.datastore.preferences.core.stringPreferencesKey("testEnumUnknown")
        dataStore.edit { settings ->
            settings[stringKey] = "INVALID_VALUE"
        }

        assertEquals(enumPref.get(), TestEnum.VALUE_A)
    }

    // Tests for Serialized (ObjectPrimitive)
    @Test
    fun serializedPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val defaultObj = SerializableObject(1, "Default")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerialized",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        assertEquals(serializedPref.get(), defaultObj)
    }

    @Test
    fun serializedPreference_setAndGetValue() = runTest(testDispatcher) {
        val defaultObj = SerializableObject(1, "Default")
        val newObj = SerializableObject(2, "New")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedSetGet",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        serializedPref.set(newObj)
        assertEquals(serializedPref.get(), newObj)
    }

    @Test
    fun serializedPreference_observeSetValue() = runTest(testDispatcher) {
        val defaultObj = SerializableObject(10, "DefaultFlow")
        val newObj = SerializableObject(20, "NewFlow")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedFlowSet",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        serializedPref.set(newObj)
        val value = serializedPref.asFlow().first()
        assertEquals(value, newObj)
    }

    @Test
    fun serializedPreference_deleteValue() = runTest(testDispatcher) {
        val defaultObj = SerializableObject(3, "DefaultDelete")
        val objToDelete = SerializableObject(4, "ToDelete")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedDelete",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        serializedPref.set(objToDelete)
        assertEquals(serializedPref.get(), objToDelete)

        serializedPref.delete()
        assertEquals(serializedPref.get(), defaultObj) // Should revert to default
    }

    // Tests for MappedPreference
    @Test
    fun mappedPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapDefault", 0)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefault",
            convert = { "MappedValue_$it" },
            reverse = { it.removePrefix("MappedValue_").toInt() },
        )
        assertEquals(mappedPref.get(), "MappedValue_0")
    }

    @Test
    fun mappedPreference_setAndGetValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapSetGet", 0)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefault",
            convert = { "MappedValue_$it" },
            reverse = { it.removePrefix("MappedValue_").toInt() },
        )
        mappedPref.set("MappedValue_100")
        assertEquals(mappedPref.get(), "MappedValue_100")
        // Also check the underlying preference
        assertEquals(intPref.get(), 100)
    }

    @Test
    fun mappedPreference_observeSetValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapObserve", 0)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultFlow",
            convert = { "FlowMapped_$it" },
            reverse = { it.removePrefix("FlowMapped_").toInt() },
        )
        mappedPref.set("FlowMapped_200")
        val value = mappedPref.asFlow().first()
        assertEquals(value, "FlowMapped_200")
        assertEquals(intPref.asFlow().first(), 200)
    }

    @Test
    fun mappedPreference_deleteValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapDelete", 50)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultDelete",
            convert = { "DeleteMapped_$it" },
            reverse = { it.removePrefix("DeleteMapped_").toInt() },
        )
        mappedPref.set("DeleteMapped_500")
        assertEquals(mappedPref.get(), "DeleteMapped_500")
        assertEquals(intPref.get(), 500)

        mappedPref.delete() // This should delete the underlying intPref
        assertEquals(
            mappedPref.get(),
            "DeleteMapped_50",
        ) // Mapped pref would return the converted default
        assertEquals(intPref.get(), 50) // Base pref should revert to its default
    }

    @Test
    fun mappedPreference_handlesConversionErrorGracefully() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapConvertError", 10)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultError",
            convert = { throw RuntimeException("Conversion Error") },
            reverse = { it.toInt() }, // Not used in this path, but required
        )

        // Set a value in the base preference so there's something to convert
        intPref.set(123)
        assertEquals(123, intPref.get())

        // When getting the mapped value, the conversion error should occur,
        // and the mapped default value should be returned.
        assertEquals("MappedDefaultError", mappedPref.get())
    }

    @Test
    fun mappedPreference_handlesReverseErrorGracefully() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapReverseError", 20)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultReverseError",
            convert = { "Converted_$it" },
            reverse = { throw RuntimeException("Reverse Conversion Error") },
        )

        // Attempt to set a value that will cause a reverse conversion error
        mappedPref.set("AttemptSet")

        // The underlying intPref should not have changed from its initial or default value
        // because the reverse conversion failed.
        assertEquals(20, intPref.get()) // Remains initial default

        // When getting the mapped value, since set failed to update the underlying pref,
        // it should try to convert the current underlying value (20)
        // or, if the set operation cleared the underlying due to error (implementation dependent),
        // it might convert the underlying default.
        // Given MappedPrefs.reverseFallback returns prefs.defaultValue,
        // the set operation on prefs would use prefs.defaultValue.
        // However, the current MappedPrefs implementation of reverseFallback in set
        // means prefs.set(prefs.defaultValue) would be called.
        // Let's refine the assertion based on MappedPrefs logic.
        // When reverse fails, prefs.set(prefs.defaultValue) is called.
        // So intPref would be set to its own default (20).
        // Then, mappedPref.get() will convert this underlying default.
        assertEquals("Converted_20", mappedPref.get())

        // Let's also test the scenario where the underlying preference *was* different
        // and then a set operation with a reverse error occurs.
        intPref.set(25) // Set underlying to a different value
        assertEquals(25, intPref.get())
        assertEquals("Converted_25", mappedPref.get())

        mappedPref.set("AttemptSetAgain") // This will cause reverse error

        // intPref should be set to its default (20) due to reverseFallback behavior
        assertEquals(20, intPref.get())
        // mappedPref should reflect the conversion of the underlying's new state (default)
        assertEquals("Converted_20", mappedPref.get())
    }
}
