package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.utils.mapIO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractBatchOperationsTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // -- batchGet tests --

    @Test
    fun batchGet_readMultiplePrimitivePreferences() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("batch_string", "hello")
        val intPref = preferenceDatastore.int("batch_int", 42)
        val boolPref = preferenceDatastore.bool("batch_bool", true)

        val result = preferenceDatastore.batchGet {
            Triple(get(stringPref), get(intPref), get(boolPref))
        }

        assertEquals("hello", result.first)
        assertEquals(42, result.second)
        assertEquals(true, result.third)
    }

    @Test
    fun batchGet_readMixedTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("batch_mixed_str", "default")
        val longPref = preferenceDatastore.long("batch_mixed_long", 100L)
        val floatPref = preferenceDatastore.float("batch_mixed_float", 1.5f)
        val doublePref = preferenceDatastore.double("batch_mixed_double", 2.5)

        stringPref.set("updated")
        longPref.set(200L)

        val result = preferenceDatastore.batchGet {
            listOf(
                get(stringPref),
                get(longPref).toString(),
                get(floatPref).toString(),
                get(doublePref).toString(),
            )
        }

        assertEquals("updated", result[0])
        assertEquals("200", result[1])
        assertEquals("1.5", result[2])
        assertEquals("2.5", result[3])
    }

    @Test
    fun batchGet_readNullablePreferences() = runTest(testDispatcher) {
        val nullableString = preferenceDatastore.nullableString("batch_nullable_str")
        val nullableInt = preferenceDatastore.nullableInt("batch_nullable_int")

        val result = preferenceDatastore.batchGet {
            Pair(get(nullableString), get(nullableInt))
        }

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun batchGet_readNullablePreferencesWithValues() = runTest(testDispatcher) {
        val nullableString = preferenceDatastore.nullableString("batch_nullable_val_str")
        val nullableInt = preferenceDatastore.nullableInt("batch_nullable_val_int")

        nullableString.set("present")
        nullableInt.set(99)

        val result = preferenceDatastore.batchGet {
            Pair(get(nullableString), get(nullableInt))
        }

        assertEquals("present", result.first)
        assertEquals(99, result.second)
    }

    @Test
    fun batchGet_readCustomSerializedPreference() = runTest(testDispatcher) {
        val customPref = preferenceDatastore.serialized(
            key = "batch_custom",
            defaultValue = "default_custom",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )

        customPref.set("hello")

        val result = preferenceDatastore.batchGet {
            get(customPref)
        }

        assertEquals("hello", result)
    }

    @Test
    fun batchGet_readSerializedSetPreference() = runTest(testDispatcher) {
        val setPref = preferenceDatastore.serializedSet(
            key = "batch_set",
            defaultValue = emptySet(),
            serializer = { it.toString() },
            deserializer = { it.toInt() },
        )

        setPref.set(setOf(1, 2, 3))

        val result = preferenceDatastore.batchGet {
            get(setPref)
        }

        assertEquals(setOf(1, 2, 3), result)
    }

    @Test
    fun batchGet_indexOperatorSyntax() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("batch_idx_get", "indexed")

        val result = preferenceDatastore.batchGet {
            this[pref]
        }

        assertEquals("indexed", result)
    }

    // -- batchReadFlow tests --

    @Test
    fun batchReadFlow_emitsOnChange() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("batch_flow_str", "before")

        val initial = preferenceDatastore
            .batchReadFlow {
                get(stringPref)
            }
            .first()
        assertEquals("before", initial)

        stringPref.set("after")

        val updated = preferenceDatastore
            .batchReadFlow {
                get(stringPref)
            }
            .first()
        assertEquals("after", updated)
    }

    // -- batchWrite tests --

    @Test
    fun batchWrite_writeMultiplePreferences() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("batch_w_str", "default")
        val intPref = preferenceDatastore.int("batch_w_int", 0)
        val boolPref = preferenceDatastore.bool("batch_w_bool", false)

        preferenceDatastore.batchWrite {
            set(stringPref, "written")
            set(intPref, 123)
            set(boolPref, true)
        }

        assertEquals("written", stringPref.get())
        assertEquals(123, intPref.get())
        assertEquals(true, boolPref.get())
    }

    @Test
    fun batchWrite_indexOperatorSyntax() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("batch_idx_set", "default")

        preferenceDatastore.batchWrite {
            this[pref] = "via_operator"
        }

        assertEquals("via_operator", pref.get())
    }

    @Test
    fun batchWrite_deleteAndSetInSameTransaction() = runTest(testDispatcher) {
        val pref1 = preferenceDatastore.string("batch_ds_1", "default1")
        val pref2 = preferenceDatastore.string("batch_ds_2", "default2")

        pref1.set("existing1")
        pref2.set("existing2")

        preferenceDatastore.batchWrite {
            delete(pref1)
            set(pref2, "new2")
        }

        assertEquals("default1", pref1.get())
        assertEquals("new2", pref2.get())
    }

    @Test
    fun batchWrite_resetToDefaultInBatch() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("batch_reset", 42)
        pref.set(99)

        preferenceDatastore.batchWrite {
            resetToDefault(pref)
        }

        assertEquals(42, pref.get())
    }

    @Test
    fun batchWrite_nullableSetAndDeleteInTransaction() = runTest(testDispatcher) {
        val nullablePref = preferenceDatastore.nullableString("batch_w_nullable")

        preferenceDatastore.batchWrite {
            set(nullablePref, "value")
        }
        assertEquals("value", nullablePref.get())

        preferenceDatastore.batchWrite {
            set(nullablePref, null)
        }
        assertNull(nullablePref.get())
    }

    @Test
    fun batchWrite_customSerializedInBatch() = runTest(testDispatcher) {
        val customPref = preferenceDatastore.serialized(
            key = "batch_w_custom",
            defaultValue = "",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )

        preferenceDatastore.batchWrite {
            set(customPref, "hello")
        }

        assertEquals("hello", customPref.get())
    }

    // -- batchUpdate tests --

    @Test
    fun batchUpdate_readAndWriteAtomically() = runTest(testDispatcher) {
        val counter = preferenceDatastore.int("batch_u_counter", 10)
        val label = preferenceDatastore.string("batch_u_label", "count:")

        preferenceDatastore.batchUpdate {
            val currentCount = get(counter)
            val currentLabel = get(label)
            set(counter, currentCount + 5)
            set(label, "$currentLabel $currentCount")
        }

        assertEquals(15, counter.get())
        assertEquals("count: 10", label.get())
    }

    @Test
    fun batchUpdate_transformFunction() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("batch_u_transform", 10)

        preferenceDatastore.batchUpdate {
            update(pref) { it * 3 }
        }

        assertEquals(30, pref.get())
    }

    @Test
    fun batchUpdate_indexOperatorSyntax() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("batch_u_idx", 5)

        preferenceDatastore.batchUpdate {
            val current = this[pref]
            this[pref] = current * 4
        }

        assertEquals(20, pref.get())
    }

    @Test
    fun batchUpdate_deleteInUpdateScope() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("batch_u_delete", "default_val")
        pref.set("set_value")

        preferenceDatastore.batchUpdate {
            delete(pref)
        }

        assertEquals("default_val", pref.get())
    }

    @Test
    fun batchUpdate_resetToDefaultInUpdateScope() = runTest(testDispatcher) {
        val pref = preferenceDatastore.int("batch_u_reset", 42)
        pref.set(99)

        preferenceDatastore.batchUpdate {
            resetToDefault(pref)
        }

        assertEquals(42, pref.get())
    }

    @Test
    fun batchUpdate_consistentSnapshotValues() = runTest(testDispatcher) {
        val a = preferenceDatastore.int("batch_u_a", 1)
        val b = preferenceDatastore.int("batch_u_b", 2)

        a.set(10)
        b.set(20)

        preferenceDatastore.batchUpdate {
            val sum = get(a) + get(b)
            set(a, sum)
            set(b, sum)
        }

        assertEquals(30, a.get())
        assertEquals(30, b.get())
    }

    // -- mapped preferences in batch --

    @Test
    fun batchGet_mappedPreferenceWorks() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("batch_mapped", 10)
        val mappedPref = intPref.mapIO(
            convert = { v: Int -> v.toString() },
            reverse = { s: String -> s.toInt() },
        )

        intPref.set(42)

        val result = preferenceDatastore.batchGet {
            get(mappedPref)
        }

        assertEquals("42", result)
    }

    @Test
    fun batchWrite_mappedPreferenceWorks() = runTest(testDispatcher) {
        val intPref = preferenceDatastore.int("batch_w_mapped", 0)
        val mappedPref = intPref.mapIO(
            convert = { v: Int -> v.toString() },
            reverse = { s: String -> s.toInt() },
        )

        preferenceDatastore.batchWrite {
            set(mappedPref, "99")
        }

        assertEquals(99, intPref.get())
    }

    // -- stringSet in batch --

    @Test
    fun batchWrite_stringSetPreference() = runTest(testDispatcher) {
        val setPref = preferenceDatastore.stringSet("batch_w_strset", emptySet())

        preferenceDatastore.batchWrite {
            set(setPref, setOf("a", "b", "c"))
        }

        assertEquals(setOf("a", "b", "c"), setPref.get())
    }
}
