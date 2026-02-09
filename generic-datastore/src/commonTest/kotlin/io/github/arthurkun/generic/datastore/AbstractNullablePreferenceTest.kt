package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullablePreferenceTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableString_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableString_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringSetGet")
        pref.set("hello")
        assertEquals("hello", pref.get())
    }

    @Test
    fun nullableString_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringSetNull")
        pref.set("hello")
        assertEquals("hello", pref.get())
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableString_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringDelete")
        pref.set("value")
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableString_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringFlow")
        val value = pref.asFlow().first()
        assertNull(value)
    }

    @Test
    fun nullableString_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringFlowSet")
        pref.set("observed")
        val value = pref.asFlow().first()
        assertEquals("observed", value)
    }

    @Test
    fun nullableString_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringUpdateNull")
        pref.update { current -> (current ?: "") + "appended" }
        assertEquals("appended", pref.get())
    }

    @Test
    fun nullableString_updateExistingValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringUpdateExisting")
        pref.set("hello")
        pref.update { current -> current + " world" }
        assertEquals("hello world", pref.get())
    }

    @Test
    fun nullableInt_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableInt("nullableIntDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableInt_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableInt("nullableIntSetGet")
        pref.set(42)
        assertEquals(42, pref.get())
    }

    @Test
    fun nullableInt_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableInt("nullableIntSetNull")
        pref.set(42)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableInt_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableInt("nullableIntDelete")
        pref.set(10)
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableInt_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableInt("nullableIntUpdateNull")
        pref.update { current -> (current ?: 0) + 5 }
        assertEquals(5, pref.get())
    }

    @Test
    fun nullableLong_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableLong("nullableLongDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableLong_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableLong("nullableLongSetGet")
        pref.set(100L)
        assertEquals(100L, pref.get())
    }

    @Test
    fun nullableLong_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableLong("nullableLongSetNull")
        pref.set(100L)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableLong_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableLong("nullableLongDelete")
        pref.set(200L)
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableFloat_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableFloat("nullableFloatDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableFloat_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableFloat("nullableFloatSetGet")
        pref.set(1.5f)
        assertEquals(1.5f, pref.get())
    }

    @Test
    fun nullableFloat_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableFloat("nullableFloatSetNull")
        pref.set(1.5f)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableFloat_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableFloat("nullableFloatDelete")
        pref.set(2.5f)
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableDouble_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableDouble("nullableDoubleDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableDouble_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableDouble("nullableDoubleSetGet")
        pref.set(3.14)
        assertEquals(3.14, pref.get())
    }

    @Test
    fun nullableDouble_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableDouble("nullableDoubleSetNull")
        pref.set(3.14)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableDouble_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableDouble("nullableDoubleDelete")
        pref.set(2.71)
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableBool_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableBool("nullableBoolDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableBool_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableBool("nullableBoolSetGet")
        pref.set(true)
        assertEquals(true, pref.get())
    }

    @Test
    fun nullableBool_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableBool("nullableBoolSetNull")
        pref.set(true)
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableBool_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableBool("nullableBoolDelete")
        pref.set(false)
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableString_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableString("nullableStringResetDefault")
        pref.set("value")
        assertEquals("value", pref.get())
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableInt_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableInt("nullableIntResetDefault")
        pref.set(99)
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableStringSet_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableStringSet_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetSetGet")
        val value = setOf("a", "b", "c")
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun nullableStringSet_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetSetNull")
        pref.set(setOf("hello"))
        assertEquals(setOf("hello"), pref.get())
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableStringSet_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetDelete")
        pref.set(setOf("value"))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableStringSet_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetFlowDefault")
        val value = pref.asFlow().first()
        assertNull(value)
    }

    @Test
    fun nullableStringSet_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetFlowSet")
        val expected = setOf("observed", "values")
        pref.set(expected)
        val value = pref.asFlow().first()
        assertEquals(expected, value)
    }

    @Test
    fun nullableStringSet_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetUpdateNull")
        pref.update { current -> (current ?: emptySet()) + "added" }
        assertEquals(setOf("added"), pref.get())
    }

    @Test
    fun nullableStringSet_updateExistingValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetUpdateExisting")
        pref.set(setOf("a", "b"))
        pref.update { current -> current?.plus("c") }
        assertEquals(setOf("a", "b", "c"), pref.get())
    }

    @Test
    fun nullableStringSet_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetResetDefault")
        pref.set(setOf("value"))
        assertEquals(setOf("value"), pref.get())
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableStringSet_emptySet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableStringSet("nullableStringSetEmpty")
        pref.set(emptySet())
        assertEquals(emptySet(), pref.get())
    }
}
