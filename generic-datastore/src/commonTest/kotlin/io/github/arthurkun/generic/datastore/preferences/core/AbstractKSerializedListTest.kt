package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.kserializedList
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractKSerializedListTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedListPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListDefault")
        assertEquals(emptyList(), pref.get())
    }

    @Test
    fun kserializedListPreference_defaultValueCustom() = runTest(testDispatcher) {
        val default = listOf(KSerUser(name = "Alice", age = 30))
        val pref = preferenceDatastore.kserializedList("testKSerListDefaultCustom", default)
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedListPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListSetGet")
        val users = listOf(
            KSerUser(name = "Alice", age = 30),
            KSerUser(name = "Bob", age = 25),
        )
        pref.set(users)
        assertEquals(users, pref.get())
    }

    @Test
    fun kserializedListPreference_preservesOrder() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListOrder")
        val users = listOf(
            KSerUser(name = "Charlie", age = 40),
            KSerUser(name = "Alice", age = 30),
            KSerUser(name = "Bob", age = 25),
        )
        pref.set(users)
        assertEquals(users, pref.get())
    }

    @Test
    fun kserializedListPreference_allowsDuplicates() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListDuplicates")
        val user = KSerUser(name = "Alice", age = 30)
        val users = listOf(user, user, user)
        pref.set(users)
        assertEquals(users, pref.get())
    }

    @Test
    fun kserializedListPreference_observeDefaultValue() = runTest(testDispatcher) {
        val default = listOf(KSerUser(name = "FlowDefault", age = 1))
        val pref = preferenceDatastore.kserializedList("testKSerListFlowDefault", default)
        val value = pref.asFlow().first()
        assertEquals(default, value)
    }

    @Test
    fun kserializedListPreference_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListFlowSet")
        val users = listOf(KSerUser(name = "Charlie", age = 40))
        pref.set(users)
        val value = pref.asFlow().first()
        assertEquals(users, value)
    }

    @Test
    fun kserializedListPreference_deleteValue() = runTest(testDispatcher) {
        val default = listOf(KSerUser(name = "Default", age = 0))
        val pref = preferenceDatastore.kserializedList("testKSerListDelete", default)
        pref.set(listOf(KSerUser(name = "ToDelete", age = 99)))
        assertEquals(listOf(KSerUser(name = "ToDelete", age = 99)), pref.get())

        pref.delete()
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedListPreference_updateValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList(
            "testKSerListUpdate",
            listOf(KSerUser(name = "Start", age = 1)),
        )
        pref.update { current -> current + KSerUser(name = "Added", age = 2) }
        assertEquals(
            listOf(KSerUser(name = "Start", age = 1), KSerUser(name = "Added", age = 2)),
            pref.get(),
        )
    }

    @Test
    fun kserializedListPreference_resetToDefault() = runTest(testDispatcher) {
        val default = listOf(KSerUser(name = "Default", age = 5))
        val pref = preferenceDatastore.kserializedList("testKSerListReset", default)
        pref.set(listOf(KSerUser(name = "Changed", age = 99)))
        assertEquals(listOf(KSerUser(name = "Changed", age = 99)), pref.get())

        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedListPreference_handleCorruptedDataReturnsDefault() = runTest(testDispatcher) {
        val default = listOf(KSerUser(name = "Safe", age = 0))
        val pref = preferenceDatastore.kserializedList("testKSerListCorrupt", default)

        val stringKey = stringPreferencesKey("testKSerListCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "NOT_VALID_JSON"
        }

        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedListPreference_differentSerializableType() = runTest(testDispatcher) {
        val addresses = listOf(
            KSerAddress(street = "123 Main St", city = "Springfield", zip = "62701"),
            KSerAddress(street = "456 Oak Ave", city = "Shelbyville", zip = "62702"),
        )
        val pref = preferenceDatastore.kserializedList<KSerAddress>("testKSerListAddress")
        pref.set(addresses)
        assertEquals(addresses, pref.get())
    }

    @Test
    fun kserializedListPreference_stateIn() = runTest(testDispatcher) {
        val default = listOf(KSerUser(name = "StateIn", age = 7))
        val pref = preferenceDatastore.kserializedList("testKSerListStateIn", default)
        val childScope = this + Job()
        val stateFlow = pref.stateIn(childScope)
        assertEquals(default, stateFlow.value)

        val updated = listOf(KSerUser(name = "NewState", age = 8))
        pref.set(updated)
        val value = stateFlow.first { it.any { u -> u.name == "NewState" } }
        assertEquals(updated, value)
        childScope.cancel()
    }

    @Test
    fun kserializedListPreference_emptyList() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListEmpty")
        pref.set(emptyList())
        assertEquals(emptyList(), pref.get())
    }

    @Test
    fun kserializedListPreference_ignoresUnknownKeys() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedList<KSerUser>("testKSerListUnknownKeys")

        val stringKey = stringPreferencesKey("testKSerListUnknownKeys")
        dataStore.edit { settings ->
            settings[stringKey] = """[{"name":"Test","age":5,"unknownField":"value"}]"""
        }

        assertEquals(listOf(KSerUser(name = "Test", age = 5)), pref.get())
    }
}
