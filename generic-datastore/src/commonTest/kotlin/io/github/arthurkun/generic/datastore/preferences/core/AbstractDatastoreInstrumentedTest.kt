package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.core.distinctFlow
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.custom.enum
import io.github.arthurkun.generic.datastore.preferences.core.customSet.enumSet
import io.github.arthurkun.generic.datastore.preferences.toggle
import io.github.arthurkun.generic.datastore.preferences.utils.map
import io.github.arthurkun.generic.datastore.preferences.utils.mapIO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

enum class TestEnum { VALUE_A, VALUE_B, VALUE_C }

data class SerializableObject(val id: Int, val name: String)

abstract class AbstractDatastoreInstrumentedTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

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
    fun stringPreference_deleteValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringDelete", "defaultValue")
        stringPref.set("valueToDelete")
        assertEquals(stringPref.get(), "valueToDelete")
        stringPref.delete()
        assertEquals(stringPref.get(), "defaultValue")
    }

    @Test
    fun stringPreference_updateValue() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringUpdate", "hello")
        stringPref.update { current -> "$current world" }
        assertEquals("hello world", stringPref.get())
    }

    @Test
    fun intPreference_updateValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testIntUpdate", 10)
        intPref.update { it + 5 }
        assertEquals(15, intPref.get())
    }

    @Test
    fun intPreference_updateFromDefault() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testIntUpdateDefault", 42)
        intPref.update { it * 2 }
        assertEquals(84, intPref.get())
    }

    @Test
    fun longPreference_updateValue() = runTest(testDispatcher) {
        val longPref = preferenceDatastore.long("testLongUpdate", 100L)
        longPref.set(200L)
        longPref.update { it + 50L }
        assertEquals(250L, longPref.get())
    }

    @Test
    fun doublePreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val doublePref = preferenceDatastore.double("testDouble", 1.0)
        assertEquals(1.0, doublePref.get())
    }

    @Test
    fun doublePreference_setAndGetValue() = runTest(testDispatcher) {
        val doublePref = preferenceDatastore.double("testDouble", 1.0)
        doublePref.set(2.5)
        assertEquals(2.5, doublePref.get())
    }

    @Test
    fun doublePreference_observeDefaultValue() = runTest(testDispatcher) {
        val doublePref = preferenceDatastore.double("testDoubleFlow", 3.14)
        val value = doublePref.asFlow().first()
        assertEquals(3.14, value)
    }

    @Test
    fun doublePreference_observeSetValue() = runTest(testDispatcher) {
        val doublePref = preferenceDatastore.double("testDoubleFlowSet", 0.0)
        doublePref.set(9.99)
        val value = doublePref.asFlow().first()
        assertEquals(9.99, value)
    }

    @Test
    fun doublePreference_deleteValue() = runTest(testDispatcher) {
        val doublePref = preferenceDatastore.double("testDoubleDelete", 1.0)
        doublePref.set(2.0)
        assertEquals(2.0, doublePref.get())
        doublePref.delete()
        assertEquals(1.0, doublePref.get())
    }

    @Test
    fun doublePreference_updateValue() = runTest(testDispatcher) {
        val doublePref = preferenceDatastore.double("testDoubleUpdate", 1.0)
        doublePref.update { it + 0.5 }
        assertEquals(1.5, doublePref.get())
    }

    @Test
    fun floatPreference_updateValue() = runTest(testDispatcher) {
        val floatPref = preferenceDatastore.float("testFloatUpdate", 1.0f)
        floatPref.update { it + 0.5f }
        assertEquals(1.5f, floatPref.get())
    }

    @Test
    fun booleanPreference_updateValue() = runTest(testDispatcher) {
        val boolPref = preferenceDatastore.bool("testBoolUpdate", false)
        boolPref.update { !it }
        assertEquals(true, boolPref.get())
    }

    @Test
    fun stringSetPreference_updateValue() = runTest(testDispatcher) {
        val stringSetPref = preferenceDatastore.stringSet("testStringSetUpdate", setOf("a"))
        stringSetPref.update { it + "b" }
        assertEquals(setOf("a", "b"), stringSetPref.get())
    }

    @Test
    fun serializedPreference_updateValue() = runTest(testDispatcher) {
        val defaultObj = SerializableObject(1, "Default")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedUpdate",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        serializedPref.update { it.copy(id = it.id + 1, name = "Updated") }
        assertEquals(SerializableObject(2, "Updated"), serializedPref.get())
    }

    @Test
    fun mappedPreference_updateValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapUpdate", 10)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefault",
            convert = { "Mapped_$it" },
            reverse = { it.removePrefix("Mapped_").toInt() },
        )
        mappedPref.update { current -> "Mapped_${current.removePrefix("Mapped_").toInt() + 5}" }
        assertEquals("Mapped_15", mappedPref.get())
        assertEquals(15, intPref.get())
    }

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
        val enumPref = preferenceDatastore.enum("testEnumUnknown", TestEnum.VALUE_A)
        enumPref.set(TestEnum.VALUE_B)
        assertEquals(enumPref.get(), TestEnum.VALUE_B)

        val stringKey = stringPreferencesKey("testEnumUnknown")
        dataStore.edit { settings ->
            settings[stringKey] = "INVALID_VALUE"
        }

        assertEquals(TestEnum.VALUE_A, enumPref.get())
    }

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
        assertEquals(serializedPref.get(), defaultObj)
    }

    @Test
    fun stringPreference_resetToDefault() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringReset", "defaultValueReset")
        stringPref.set("valueToReset")
        assertEquals(stringPref.get(), "valueToReset")

        stringPref.resetToDefault()
        assertEquals(stringPref.get(), "defaultValueReset")
    }

    @Test
    fun intPreference_resetToDefault() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("testIntReset", 10)
        intPref.set(20)
        assertEquals(intPref.get(), 20)

        intPref.resetToDefault()
        assertEquals(intPref.get(), 10)
    }

    @Test
    fun longPreference_resetToDefault() = runTest(testDispatcher) {
        val longPref = preferenceDatastore.long("testLongReset", 100L)
        longPref.set(200L)
        assertEquals(longPref.get(), 200L)

        longPref.resetToDefault()
        assertEquals(longPref.get(), 100L)
    }

    @Test
    fun floatPreference_resetToDefault() = runTest(testDispatcher) {
        val floatPref = preferenceDatastore.float("testFloatReset", 1.0f)
        floatPref.set(2.0f)
        assertEquals(floatPref.get(), 2.0f)

        floatPref.resetToDefault()
        assertEquals(floatPref.get(), 1.0f)
    }

    @Test
    fun booleanPreference_resetToDefault() = runTest(testDispatcher) {
        val boolPref = preferenceDatastore.bool("testBooleanReset", false)
        boolPref.set(true)
        assertEquals(boolPref.get(), true)

        boolPref.resetToDefault()
        assertEquals(boolPref.get(), false)
    }

    @Test
    fun stringSetPreference_resetToDefault() = runTest(testDispatcher) {
        val stringSetPref = preferenceDatastore.stringSet("testStringSetReset", setOf("a", "b"))
        stringSetPref.set(setOf("c", "d", "e"))
        assertEquals(stringSetPref.get(), setOf("c", "d", "e"))

        stringSetPref.resetToDefault()
        assertEquals(stringSetPref.get(), setOf("a", "b"))
    }

    @Test
    fun enumPreference_resetToDefault() = runTest(testDispatcher) {
        val enumPref = preferenceDatastore.enum("testEnumReset", TestEnum.VALUE_A)
        enumPref.set(TestEnum.VALUE_B)
        assertEquals(enumPref.get(), TestEnum.VALUE_B)

        enumPref.resetToDefault()
        assertEquals(enumPref.get(), TestEnum.VALUE_A)
    }

    @Test
    fun serializedPreference_resetToDefault() = runTest(testDispatcher) {
        val defaultObj = SerializableObject(5, "DefaultReset")
        val objToReset = SerializableObject(6, "ToReset")
        val serializedPref = preferenceDatastore.serialized(
            key = "testSerializedReset",
            defaultValue = defaultObj,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        serializedPref.set(objToReset)
        assertEquals(serializedPref.get(), objToReset)

        serializedPref.resetToDefault()
        assertEquals(serializedPref.get(), defaultObj)
    }

    @Test
    fun stringPreference_resetToDefault_whenNeverSet() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringResetNeverSet", "defaultValue")
        stringPref.resetToDefault()
        assertEquals(stringPref.get(), "defaultValue")
    }

    @Test
    fun stringPreference_resetToDefault_multipleTimes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("testStringResetMultiple", "defaultMultiple")
        stringPref.set("changed")
        stringPref.resetToDefault()
        stringPref.resetToDefault()
        assertEquals(stringPref.get(), "defaultMultiple")
    }

    @Test
    fun serializedSetPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetDefault",
            defaultValue = setOf(SerializableObject(1, "A"), SerializableObject(2, "B")),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        assertEquals(setOf(SerializableObject(1, "A"), SerializableObject(2, "B")), pref.get())
    }

    @Test
    fun serializedSetPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetSetGet",
            defaultValue = emptySet(),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        val newSet = setOf(SerializableObject(3, "C"), SerializableObject(4, "D"))
        pref.set(newSet)
        assertEquals(newSet, pref.get())
    }

    @Test
    fun serializedSetPreference_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetFlow",
            defaultValue = emptySet(),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        val newSet = setOf(SerializableObject(5, "E"))
        pref.set(newSet)
        val value = pref.asFlow().first()
        assertEquals(newSet, value)
    }

    @Test
    fun serializedSetPreference_deleteValue() = runTest(testDispatcher) {
        val defaultSet = setOf(SerializableObject(1, "Default"))
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetDelete",
            defaultValue = defaultSet,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.set(setOf(SerializableObject(9, "ToDelete")))
        assertEquals(setOf(SerializableObject(9, "ToDelete")), pref.get())

        pref.delete()
        assertEquals(defaultSet, pref.get())
    }

    @Test
    fun serializedSetPreference_updateValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetUpdate",
            defaultValue = setOf(SerializableObject(1, "A")),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.update { it + SerializableObject(2, "B") }
        assertEquals(setOf(SerializableObject(1, "A"), SerializableObject(2, "B")), pref.get())
    }

    @Test
    fun serializedSetPreference_resetToDefault() = runTest(testDispatcher) {
        val defaultSet = setOf(SerializableObject(1, "Default"))
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetReset",
            defaultValue = defaultSet,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.set(setOf(SerializableObject(99, "Changed")))
        assertEquals(setOf(SerializableObject(99, "Changed")), pref.get())

        pref.resetToDefault()
        assertEquals(defaultSet, pref.get())
    }

    @Test
    fun serializedSetPreference_handleDeserializationErrorSkipsInvalidElements() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetDeserError",
            defaultValue = emptySet(),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )

        val stringSetKey = stringSetPreferencesKey("testSerializedSetDeserError")
        dataStore.edit { settings ->
            settings[stringSetKey] = setOf("1,Valid", "INVALID_DATA")
        }

        val result = pref.get()
        assertEquals(setOf(SerializableObject(1, "Valid")), result)
    }

    @Test
    fun enumSetPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet("testEnumSetDefault", setOf(TestEnum.VALUE_A, TestEnum.VALUE_B))
        assertEquals(setOf(TestEnum.VALUE_A, TestEnum.VALUE_B), pref.get())
    }

    @Test
    fun enumSetPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<TestEnum>("testEnumSetSetGet")
        pref.set(setOf(TestEnum.VALUE_B, TestEnum.VALUE_C))
        assertEquals(setOf(TestEnum.VALUE_B, TestEnum.VALUE_C), pref.get())
    }

    @Test
    fun enumSetPreference_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<TestEnum>("testEnumSetFlow")
        pref.set(setOf(TestEnum.VALUE_A))
        val value = pref.asFlow().first()
        assertEquals(setOf(TestEnum.VALUE_A), value)
    }

    @Test
    fun enumSetPreference_deleteValue() = runTest(testDispatcher) {
        val defaultSet = setOf(TestEnum.VALUE_A)
        val pref = preferenceDatastore.enumSet("testEnumSetDelete", defaultSet)
        pref.set(setOf(TestEnum.VALUE_C))
        assertEquals(setOf(TestEnum.VALUE_C), pref.get())

        pref.delete()
        assertEquals(defaultSet, pref.get())
    }

    @Test
    fun enumSetPreference_updateValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet("testEnumSetUpdate", setOf(TestEnum.VALUE_A))
        pref.update { it + TestEnum.VALUE_B }
        assertEquals(setOf(TestEnum.VALUE_A, TestEnum.VALUE_B), pref.get())
    }

    @Test
    fun enumSetPreference_resetToDefault() = runTest(testDispatcher) {
        val defaultSet = setOf(TestEnum.VALUE_A, TestEnum.VALUE_B)
        val pref = preferenceDatastore.enumSet("testEnumSetReset", defaultSet)
        pref.set(setOf(TestEnum.VALUE_C))
        assertEquals(setOf(TestEnum.VALUE_C), pref.get())

        pref.resetToDefault()
        assertEquals(defaultSet, pref.get())
    }

    @Test
    fun enumSetPreference_handleUnknownValueSkipsInvalidElements() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<TestEnum>("testEnumSetUnknown")

        val stringSetKey = stringSetPreferencesKey("testEnumSetUnknown")
        dataStore.edit { settings ->
            settings[stringSetKey] = setOf("VALUE_A", "INVALID_VALUE", "VALUE_C")
        }

        val result = pref.get()
        assertEquals(setOf(TestEnum.VALUE_A, TestEnum.VALUE_C), result)
    }

    @Test
    fun enumSetPreference_emptySet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<TestEnum>("testEnumSetEmpty")
        pref.set(emptySet())
        assertEquals(emptySet(), pref.get())
    }

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

        mappedPref.delete()
        assertEquals(
            mappedPref.get(),
            "DeleteMapped_50",
        )
        assertEquals(intPref.get(), 50)
    }

    @Test
    fun mappedPreference_resetToDefault() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapReset", 75)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultReset",
            convert = { "ResetMapped_$it" },
            reverse = { it.removePrefix("ResetMapped_").toInt() },
        )
        mappedPref.set("ResetMapped_750")
        assertEquals(mappedPref.get(), "ResetMapped_750")
        assertEquals(intPref.get(), 750)

        mappedPref.resetToDefault()
        assertEquals(mappedPref.get(), "ResetMapped_75")
        assertEquals(intPref.get(), 75)
    }

    @Test
    fun mappedPreference_resetToDefault_resetsUnderlyingPreference() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapResetUnderlying", 10)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefault",
            convert = { "Mapped_$it" },
            reverse = { it.removePrefix("Mapped_").toInt() },
        )
        intPref.set(999)
        assertEquals(intPref.get(), 999)

        mappedPref.resetToDefault()
        assertEquals(intPref.get(), 10)
        assertEquals(mappedPref.get(), "Mapped_10")
    }

    @Test
    fun mappedPreference_handlesConversionErrorGracefully() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapConvertError", 10)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultError",
            convert = { throw RuntimeException("Conversion Error") },
            reverse = { it.toInt() },
        )

        intPref.set(123)
        assertEquals(123, intPref.get())

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

        mappedPref.set("AttemptSet")

        assertEquals(20, intPref.get())
        assertEquals("Converted_20", mappedPref.get())

        intPref.set(25)
        assertEquals(25, intPref.get())
        assertEquals("Converted_25", mappedPref.get())

        mappedPref.set("AttemptSetAgain")

        assertEquals(20, intPref.get())
        assertEquals("Converted_20", mappedPref.get())
    }

    @Test
    fun mappedPreference_io_defaultValueInferred() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapIoDefault", 7)
        val mappedPref = intPref.mapIO(
            convert = { "Io_$it" },
            reverse = { it.removePrefix("Io_").toInt() },
        )

        assertEquals("Io_7", mappedPref.get())

        mappedPref.set("Io_9")
        assertEquals(9, intPref.get())
        assertEquals("Io_9", mappedPref.get())
    }

    @Test
    fun mappedPreference_io_usesFallbackWhenConvertFails() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapIoConvertFallback", 0)
        val mappedPref = intPref.mapIO(
            convert = { value ->
                if (value == 0) {
                    "Zero"
                } else {
                    throw IllegalStateException("Bad value")
                }
            },
            reverse = { value ->
                if (value == "Zero") {
                    0
                } else {
                    value.toInt()
                }
            },
        )

        intPref.set(1)

        assertEquals("Zero", mappedPref.get())
    }

    @Test
    fun mappedPreference_io_usesFallbackWhenReverseFails() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapIoReverseFallback", 0)
        val mappedPref = intPref.mapIO(
            convert = { "Zero" },
            reverse = { value ->
                if (value == "Zero") {
                    0
                } else {
                    throw IllegalStateException("Bad value")
                }
            },
        )

        mappedPref.set("Bad")

        assertEquals(0, intPref.get())
        assertEquals("Zero", mappedPref.get())
    }

    @Test
    fun mappedPreference_io_throwsWhenDefaultConversionFails() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("baseForMapIoDefaultFail", 3)

        assertFailsWith<IllegalStateException> {
            intPref.mapIO(
                convert = { throw IllegalStateException("Default conversion failed") },
                reverse = { it.toInt() },
            )
        }
    }

    @Test
    fun stringSetPreference_toggleAddsItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringSet("testStringSetToggleAdd", setOf("a"))
        pref.toggle("b")
        assertEquals(setOf("a", "b"), pref.get())
    }

    @Test
    fun stringSetPreference_toggleRemovesItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringSet("testStringSetToggleRemove", setOf("a", "b"))
        pref.toggle("b")
        assertEquals(setOf("a"), pref.get())
    }

    @Test
    fun stringSetPreference_toggleOnEmptySetAddsItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringSet("testStringSetToggleEmpty")
        pref.toggle("a")
        assertEquals(setOf("a"), pref.get())
    }

    @Test
    fun stringSetPreference_toggleSameItemTwiceRestoresOriginal() = runTest(testDispatcher) {
        val pref = preferenceDatastore.stringSet("testStringSetToggleTwice", setOf("a"))
        pref.toggle("b")
        assertEquals(setOf("a", "b"), pref.get())
        pref.toggle("b")
        assertEquals(setOf("a"), pref.get())
    }

    @Test
    fun enumSetPreference_toggleAddsItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet("testEnumSetToggleAdd", setOf(TestEnum.VALUE_A))
        pref.toggle(TestEnum.VALUE_B)
        assertEquals(setOf(TestEnum.VALUE_A, TestEnum.VALUE_B), pref.get())
    }

    @Test
    fun enumSetPreference_toggleRemovesItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet("testEnumSetToggleRemove", setOf(TestEnum.VALUE_A, TestEnum.VALUE_B))
        pref.toggle(TestEnum.VALUE_A)
        assertEquals(setOf(TestEnum.VALUE_B), pref.get())
    }

    @Test
    fun enumSetPreference_toggleOnEmptySetAddsItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet<TestEnum>("testEnumSetToggleEmpty")
        pref.toggle(TestEnum.VALUE_C)
        assertEquals(setOf(TestEnum.VALUE_C), pref.get())
    }

    @Test
    fun enumSetPreference_toggleSameItemTwiceRestoresOriginal() = runTest(testDispatcher) {
        val pref = preferenceDatastore.enumSet("testEnumSetToggleTwice", setOf(TestEnum.VALUE_A))
        pref.toggle(TestEnum.VALUE_B)
        assertEquals(setOf(TestEnum.VALUE_A, TestEnum.VALUE_B), pref.get())
        pref.toggle(TestEnum.VALUE_B)
        assertEquals(setOf(TestEnum.VALUE_A), pref.get())
    }

    @Test
    fun serializedSetPreference_toggleAddsItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetToggleAdd",
            defaultValue = setOf(SerializableObject(1, "A")),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.toggle(SerializableObject(2, "B"))
        assertEquals(setOf(SerializableObject(1, "A"), SerializableObject(2, "B")), pref.get())
    }

    @Test
    fun serializedSetPreference_toggleRemovesItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetToggleRemove",
            defaultValue = setOf(SerializableObject(1, "A"), SerializableObject(2, "B")),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.toggle(SerializableObject(2, "B"))
        assertEquals(setOf(SerializableObject(1, "A")), pref.get())
    }

    @Test
    fun serializedSetPreference_toggleOnEmptySetAddsItem() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetToggleEmpty",
            defaultValue = emptySet(),
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.toggle(SerializableObject(1, "A"))
        assertEquals(setOf(SerializableObject(1, "A")), pref.get())
    }

    @Test
    fun serializedSetPreference_toggleSameItemTwiceRestoresOriginal() = runTest(testDispatcher) {
        val defaultSet = setOf(SerializableObject(1, "A"))
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetToggleTwice",
            defaultValue = defaultSet,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObject(parts[0].toInt(), parts[1])
            },
        )
        pref.toggle(SerializableObject(2, "B"))
        assertEquals(setOf(SerializableObject(1, "A"), SerializableObject(2, "B")), pref.get())
        pref.toggle(SerializableObject(2, "B"))
        assertEquals(defaultSet, pref.get())
    }

    @Test
    fun booleanPreference_toggleFromFalse() = runTest(testDispatcher) {
        val pref = preferenceDatastore.bool("testBoolToggleFalse", false)
        pref.toggle()
        assertTrue(pref.get())
    }

    @Test
    fun booleanPreference_toggleFromTrue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.bool("testBoolToggleTrue", true)
        pref.toggle()
        assertFalse(pref.get())
    }

    @Test
    fun booleanPreference_toggleTwiceRestoresOriginal() = runTest(testDispatcher) {
        val pref = preferenceDatastore.bool("testBoolToggleTwice", false)
        pref.toggle()
        assertTrue(pref.get())
        pref.toggle()
        assertFalse(pref.get())
    }

    @Test
    fun booleanPreference_toggleAfterSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.bool("testBoolToggleAfterSet", false)
        pref.set(true)
        pref.toggle()
        assertFalse(pref.get())
    }

    @Test
    fun distinctFlow_emitsDefaultValue() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("distinctFlowDefault", 42)
        val value = intPref.distinctFlow().first()
        assertEquals(42, value)
    }

    @Test
    fun distinctFlow_filtersConsecutiveDuplicates() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("distinctFlowDistinct", 0)
        intPref.set(5)
        val value = intPref.distinctFlow().first()
        assertEquals(5, value)

        intPref.set(5)
        val sameValue = intPref.distinctFlow().first()
        assertEquals(5, sameValue)

        intPref.set(10)
        val newValue = intPref.distinctFlow().first()
        assertEquals(10, newValue)
    }

    @Test
    fun distinctFlow_worksWithStringPreference() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("distinctFlowString", "initial")
        val value = stringPref.distinctFlow().first()
        assertEquals("initial", value)

        stringPref.set("changed")
        val updated = stringPref.distinctFlow().first()
        assertEquals("changed", updated)
    }

    @Test
    fun distinctFlow_worksWithBooleanPreference() = runTest(testDispatcher) {
        val boolPref = preferenceDatastore.bool("distinctFlowBool", false)
        val value = boolPref.distinctFlow().first()
        assertEquals(false, value)

        boolPref.set(true)
        val updated = boolPref.distinctFlow().first()
        assertEquals(true, updated)
    }

    @Test
    fun clearAll_resetsAllPreferencesToDefaults() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("clearAllString", "default")
        val intPref = preferenceDatastore.int("clearAllInt", 0)
        val boolPref = preferenceDatastore.bool("clearAllBool", false)

        stringPref.set("changed")
        intPref.set(42)
        boolPref.set(true)

        assertEquals("changed", stringPref.get())
        assertEquals(42, intPref.get())
        assertEquals(true, boolPref.get())

        preferenceDatastore.clearAll()

        assertEquals("default", stringPref.get())
        assertEquals(0, intPref.get())
        assertEquals(false, boolPref.get())
    }

    @Test
    fun clearAll_onEmptyDatastoreDoesNotThrow() = runTest(testDispatcher) {
        preferenceDatastore.clearAll()
        val stringPref = preferenceDatastore.string("clearAllEmpty", "default")
        assertEquals("default", stringPref.get())
    }

    @Test
    fun clearAll_preferencesCanBeSetAgainAfterClear() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("clearAllReuse", 0)
        intPref.set(10)
        preferenceDatastore.clearAll()
        assertEquals(0, intPref.get())

        intPref.set(20)
        assertEquals(20, intPref.get())
    }
}
