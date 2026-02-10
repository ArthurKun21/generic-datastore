package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.default.custom.enum
import io.github.arthurkun.generic.datastore.preferences.default.customSet.enumSet
import io.github.arthurkun.generic.datastore.preferences.utils.map
import io.github.arthurkun.generic.datastore.preferences.utils.mapIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private enum class TestEnumBlocking { VALUE_A, VALUE_B }

private data class SerializableObjectBlocking(val id: Int, val name: String)

abstract class AbstractDatastoreBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun stringPreference_resetToDefaultBlocking() {
        val stringPref = preferenceDatastore.string("testString", "defaultValueReset")
        stringPref.setBlocking("valueToReset")
        assertEquals(stringPref.getBlocking(), "valueToReset")

        stringPref.resetToDefaultBlocking()
        assertEquals(stringPref.getBlocking(), "defaultValueReset")
    }

    @Test
    fun intPreference_resetToDefaultBlocking() {
        val intPref = preferenceDatastore.int("testInt", 10)
        intPref.setBlocking(20)
        assertEquals(intPref.getBlocking(), 20)

        intPref.resetToDefaultBlocking()
        assertEquals(intPref.getBlocking(), 10)
    }

    @Test
    fun longPreference_resetToDefaultBlocking() {
        val longPref = preferenceDatastore.long("testLong", 100L)
        longPref.setBlocking(200L)
        assertEquals(longPref.getBlocking(), 200L)

        longPref.resetToDefaultBlocking()
        assertEquals(longPref.getBlocking(), 100L)
    }

    @Test
    fun floatPreference_resetToDefaultBlocking() {
        val floatPref = preferenceDatastore.float("testFloat", 1.0f)
        floatPref.setBlocking(2.0f)
        assertEquals(floatPref.getBlocking(), 2.0f)

        floatPref.resetToDefaultBlocking()
        assertEquals(floatPref.getBlocking(), 1.0f)
    }

    @Test
    fun booleanPreference_resetToDefaultBlocking() {
        val boolPref = preferenceDatastore.bool("testBoolean", false)
        boolPref.setBlocking(true)
        assertEquals(boolPref.getBlocking(), true)

        boolPref.resetToDefaultBlocking()
        assertEquals(boolPref.getBlocking(), false)
    }

    @Test
    fun stringSetPreference_resetToDefaultBlocking() {
        val stringSetPref = preferenceDatastore.stringSet("testStringSet", setOf("a", "b"))
        stringSetPref.setBlocking(setOf("c", "d", "e"))
        assertEquals(stringSetPref.getBlocking(), setOf("c", "d", "e"))

        stringSetPref.resetToDefaultBlocking()
        assertEquals(stringSetPref.getBlocking(), setOf("a", "b"))
    }

    @Test
    fun enumPreference_resetToDefaultBlocking() {
        val enumPref = preferenceDatastore.enum("testEnum", TestEnumBlocking.VALUE_A)
        enumPref.setBlocking(TestEnumBlocking.VALUE_B)
        assertEquals(enumPref.getBlocking(), TestEnumBlocking.VALUE_B)

        enumPref.resetToDefaultBlocking()
        assertEquals(enumPref.getBlocking(), TestEnumBlocking.VALUE_A)
    }

    @Test
    fun serializedPreference_resetToDefaultBlocking() {
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

        serializedPref.resetToDefaultBlocking()
        assertEquals(serializedPref.getBlocking(), defaultObj)
    }

    @Test
    fun serializedSetPreference_resetToDefaultBlocking() {
        val defaultSet = setOf(SerializableObjectBlocking(1, "A"), SerializableObjectBlocking(2, "B"))
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetResetBlocking",
            defaultValue = defaultSet,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObjectBlocking(parts[0].toInt(), parts[1])
            },
        )
        pref.setBlocking(setOf(SerializableObjectBlocking(3, "C")))
        assertEquals(setOf(SerializableObjectBlocking(3, "C")), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(defaultSet, pref.getBlocking())
    }

    @Test
    fun enumSetPreference_resetToDefaultBlocking() {
        val defaultSet = setOf(TestEnumBlocking.VALUE_A)
        val pref = preferenceDatastore.enumSet("testEnumSetResetBlocking", defaultSet)
        pref.setBlocking(setOf(TestEnumBlocking.VALUE_B))
        assertEquals(setOf(TestEnumBlocking.VALUE_B), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(defaultSet, pref.getBlocking())
    }

    @Test
    fun enumSetPreference_delegation() {
        val defaultSet = setOf(TestEnumBlocking.VALUE_A)
        val pref = preferenceDatastore.enumSet("testEnumSetDelegate", defaultSet)
        var delegatedValue: Set<TestEnumBlocking> by pref

        delegatedValue = setOf(TestEnumBlocking.VALUE_A, TestEnumBlocking.VALUE_B)
        assertEquals(setOf(TestEnumBlocking.VALUE_A, TestEnumBlocking.VALUE_B), delegatedValue)
        assertEquals(setOf(TestEnumBlocking.VALUE_A, TestEnumBlocking.VALUE_B), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(defaultSet, delegatedValue)
        assertEquals(defaultSet, pref.getBlocking())
    }

    @Test
    fun serializedSetPreference_delegation() {
        val defaultSet = setOf(SerializableObjectBlocking(1, "A"))
        val pref = preferenceDatastore.serializedSet(
            key = "testSerializedSetDelegate",
            defaultValue = defaultSet,
            serializer = { "${it.id},${it.name}" },
            deserializer = { str ->
                val parts = str.split(",", limit = 2)
                SerializableObjectBlocking(parts[0].toInt(), parts[1])
            },
        )
        var delegatedValue: Set<SerializableObjectBlocking> by pref

        val newSet = setOf(SerializableObjectBlocking(2, "B"), SerializableObjectBlocking(3, "C"))
        delegatedValue = newSet
        assertEquals(newSet, delegatedValue)
        assertEquals(newSet, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(defaultSet, delegatedValue)
        assertEquals(defaultSet, pref.getBlocking())
    }

    @Test
    fun mappedPreference_resetToDefaultBlocking() {
        val intPref = preferenceDatastore.int("baseForMapReset", 75)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefaultReset",
            convert = { "ResetMapped_$it" },
            reverse = { it.removePrefix("ResetMapped_").toInt() },
        )
        mappedPref.setBlocking("ResetMapped_750")
        assertEquals(mappedPref.getBlocking(), "ResetMapped_750")
        assertEquals(intPref.getBlocking(), 750)

        mappedPref.resetToDefaultBlocking()
        assertEquals(
            mappedPref.getBlocking(),
            "ResetMapped_75",
        )
        assertEquals(intPref.getBlocking(), 75)
    }

    @Test
    fun stringPreference_resetToDefaultBlocking_whenNeverSet() {
        val stringPref = preferenceDatastore.string("testStringResetNeverSet", "defaultValue")
        stringPref.resetToDefaultBlocking()
        assertEquals(stringPref.getBlocking(), "defaultValue")
    }

    @Test
    fun intPreference_resetToDefaultBlocking_whenNeverSet() {
        val intPref = preferenceDatastore.int("testIntResetNeverSet", 42)
        intPref.resetToDefaultBlocking()
        assertEquals(intPref.getBlocking(), 42)
    }

    @Test
    fun stringPreference_resetToDefaultBlocking_multipleTimes() {
        val stringPref = preferenceDatastore.string("testStringResetMultiple", "defaultMultiple")
        stringPref.setBlocking("changed")
        stringPref.resetToDefaultBlocking()
        stringPref.resetToDefaultBlocking()
        assertEquals(stringPref.getBlocking(), "defaultMultiple")
    }

    @Test
    fun mappedPreference_resetToDefaultBlocking_resetsUnderlyingPreference() {
        val intPref = preferenceDatastore.int("baseForMapResetUnderlying", 10)
        val mappedPref = intPref.map(
            defaultValue = "MappedDefault",
            convert = { "Mapped_$it" },
            reverse = { it.removePrefix("Mapped_").toInt() },
        )
        intPref.setBlocking(999)
        assertEquals(intPref.getBlocking(), 999)

        mappedPref.resetToDefaultBlocking()
        assertEquals(intPref.getBlocking(), 10)
        assertEquals(mappedPref.getBlocking(), "Mapped_10")
    }

    @Test
    fun stringPreference_delegation() {
        val stringPref = preferenceDatastore.string("testStringDelegate", "defaultDelegateValue")
        var delegatedValue: String by stringPref

        delegatedValue = "newDelegateValue"
        assertEquals("newDelegateValue", delegatedValue)
        assertEquals("newDelegateValue", stringPref.getBlocking())

        stringPref.resetToDefaultBlocking()
        assertEquals("defaultDelegateValue", delegatedValue)
        assertEquals("defaultDelegateValue", stringPref.getBlocking())
    }

    @Test
    fun intPreference_delegation() {
        val intPref = preferenceDatastore.int("testIntDelegate", 123)
        var delegatedValue: Int by intPref

        delegatedValue = 456
        assertEquals(456, delegatedValue)
        assertEquals(456, intPref.getBlocking())

        intPref.resetToDefaultBlocking()
        assertEquals(123, delegatedValue)
        assertEquals(123, intPref.getBlocking())
    }

    @Test
    fun longPreference_delegation() {
        val longPref = preferenceDatastore.long("testLongDelegate", 123L)
        var delegatedValue: Long by longPref

        delegatedValue = 456L
        assertEquals(456L, delegatedValue)
        assertEquals(456L, longPref.getBlocking())

        longPref.resetToDefaultBlocking()
        assertEquals(123L, delegatedValue)
        assertEquals(123L, longPref.getBlocking())
    }

    @Test
    fun floatPreference_delegation() {
        val floatPref = preferenceDatastore.float("testFloatDelegate", 1.23f)
        var delegatedValue: Float by floatPref

        delegatedValue = 4.56f
        assertEquals(4.56f, delegatedValue)
        assertEquals(4.56f, floatPref.getBlocking())

        floatPref.resetToDefaultBlocking()
        assertEquals(1.23f, delegatedValue)
        assertEquals(1.23f, floatPref.getBlocking())
    }

    @Test
    fun booleanPreference_delegation() {
        val boolPref = preferenceDatastore.bool("testBooleanDelegate", false)
        var delegatedValue: Boolean by boolPref

        delegatedValue = true
        assertEquals(true, delegatedValue)
        assertEquals(true, boolPref.getBlocking())

        boolPref.resetToDefaultBlocking()
        assertEquals(false, delegatedValue)
        assertEquals(false, boolPref.getBlocking())
    }

    @Test
    fun stringSetPreference_delegation() {
        val stringSetPref = preferenceDatastore.stringSet("testStringSetDelegate", setOf("a", "b"))
        var delegatedValue: Set<String> by stringSetPref

        delegatedValue = setOf("c", "d")
        assertEquals(setOf("c", "d"), delegatedValue)
        assertEquals(setOf("c", "d"), stringSetPref.getBlocking())

        stringSetPref.resetToDefaultBlocking()
        assertEquals(setOf("a", "b"), delegatedValue)
        assertEquals(setOf("a", "b"), stringSetPref.getBlocking())
    }

    @Test
    fun enumPreference_delegation() {
        val enumPref = preferenceDatastore.enum("testEnumDelegate", TestEnumBlocking.VALUE_A)
        var delegatedValue: TestEnumBlocking by enumPref

        delegatedValue = TestEnumBlocking.VALUE_B
        assertEquals(TestEnumBlocking.VALUE_B, delegatedValue)
        assertEquals(TestEnumBlocking.VALUE_B, enumPref.getBlocking())

        enumPref.resetToDefaultBlocking()
        assertEquals(TestEnumBlocking.VALUE_A, delegatedValue)
        assertEquals(TestEnumBlocking.VALUE_A, enumPref.getBlocking())
    }

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
            },
        )
        var delegatedValue: SerializableObjectBlocking by serializedPref

        delegatedValue = newObj
        assertEquals(newObj, delegatedValue)
        assertEquals(newObj, serializedPref.getBlocking())

        serializedPref.resetToDefaultBlocking()
        assertEquals(defaultObj, delegatedValue)
        assertEquals(defaultObj, serializedPref.getBlocking())
    }

    @Test
    fun mappedPreference_delegation() {
        val intPref = preferenceDatastore.int("baseForMapDelegate", 100)
        val mappedPref = intPref.map(
            defaultValue = "MappedDelegateDefault",
            convert = { "DelegateMapped_$it" },
            reverse = { it.removePrefix("DelegateMapped_").toInt() },
        )
        var delegatedValue: String by mappedPref

        delegatedValue = "DelegateMapped_200"
        assertEquals("DelegateMapped_200", delegatedValue)
        assertEquals("DelegateMapped_200", mappedPref.getBlocking())
        assertEquals(200, intPref.getBlocking())

        mappedPref.resetToDefaultBlocking()
        assertEquals(
            "DelegateMapped_100",
            delegatedValue,
        )
        assertEquals("DelegateMapped_100", mappedPref.getBlocking())
        assertEquals(100, intPref.getBlocking())
    }

    @Test
    fun mappedPreference_io_defaultValueInferredBlocking() {
        val intPref = preferenceDatastore.int("baseForMapIoDefaultBlocking", 11)
        val mappedPref = intPref.mapIO(
            convert = { "Io_$it" },
            reverse = { it.removePrefix("Io_").toInt() },
        )

        assertEquals("Io_11", mappedPref.getBlocking())

        mappedPref.setBlocking("Io_13")
        assertEquals(13, intPref.getBlocking())
        assertEquals("Io_13", mappedPref.getBlocking())
    }

    @Test
    fun mappedPreference_io_usesFallbackWhenConvertFailsBlocking() {
        val intPref = preferenceDatastore.int("baseForMapIoConvertFallbackBlocking", 0)
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

        intPref.setBlocking(1)

        assertEquals("Zero", mappedPref.getBlocking())
    }

    @Test
    fun mappedPreference_io_usesFallbackWhenReverseFailsBlocking() {
        val intPref = preferenceDatastore.int("baseForMapIoReverseFallbackBlocking", 0)
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

        mappedPref.setBlocking("Bad")

        assertEquals(0, intPref.getBlocking())
        assertEquals("Zero", mappedPref.getBlocking())
    }

    @Test
    fun mappedPreference_io_throwsWhenDefaultConversionFailsBlocking() {
        val intPref = preferenceDatastore.int("baseForMapIoDefaultFailBlocking", 3)

        assertFailsWith<IllegalStateException> {
            intPref.mapIO(
                convert = { throw IllegalStateException("Default conversion failed") },
                reverse = { it.toInt() },
            )
        }
    }
}
