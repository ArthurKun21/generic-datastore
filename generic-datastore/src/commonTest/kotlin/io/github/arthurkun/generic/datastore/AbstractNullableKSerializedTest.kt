package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.nullableKserialized
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableKSerializedTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableKserialized_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableKserialized_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerSetGet")
        val user = KSerUser(name = "Alice", age = 30)
        pref.set(user)
        assertEquals(user, pref.get())
    }

    @Test
    fun nullableKserialized_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerSetNull")
        pref.set(KSerUser(name = "Bob", age = 25))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserialized_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerDelete")
        pref.set(KSerUser(name = "Charlie", age = 35))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableKserialized_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerFlowDefault")
        val value = pref.asFlow().first()
        assertNull(value)
    }

    @Test
    fun nullableKserialized_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerFlowSet")
        val user = KSerUser(name = "Diana", age = 28)
        pref.set(user)
        val value = pref.asFlow().first()
        assertEquals(user, value)
    }

    @Test
    fun nullableKserialized_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerUpdateNull")
        pref.update { current -> current ?: KSerUser(name = "Default", age = 0) }
        assertEquals(KSerUser(name = "Default", age = 0), pref.get())
    }

    @Test
    fun nullableKserialized_updateExistingValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerUpdateExisting")
        pref.set(KSerUser(name = "Eve", age = 20))
        pref.update { current -> current?.copy(age = 21) }
        assertEquals(KSerUser(name = "Eve", age = 21), pref.get())
    }

    @Test
    fun nullableKserialized_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerResetDefault")
        pref.set(KSerUser(name = "Frank", age = 40))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableKserialized_corruptedDataReturnsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerUser>("nullableKSerCorrupt")
        val stringKey = stringPreferencesKey("nullableKSerCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "NOT_VALID_JSON"
        }
        assertNull(pref.get())
    }

    @Test
    fun nullableKserialized_differentType() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserialized<KSerAddress>("nullableKSerAddress")
        val address = KSerAddress(street = "123 Main St", city = "Springfield", zip = "62701")
        pref.set(address)
        assertEquals(address, pref.get())
    }
}
