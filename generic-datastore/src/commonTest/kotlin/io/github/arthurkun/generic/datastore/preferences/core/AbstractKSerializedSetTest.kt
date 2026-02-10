package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.kserializedSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractKSerializedSetTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedSetPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedSet<KSerUser>("testKSerSetDefault")
        assertEquals(emptySet(), pref.get())
    }

    @Test
    fun kserializedSetPreference_defaultValueCustom() = runTest(testDispatcher) {
        val default = setOf(KSerUser(name = "Alice", age = 30))
        val pref = preferenceDatastore.kserializedSet("testKSerSetDefaultCustom", default)
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedSetPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedSet<KSerUser>("testKSerSetSetGet")
        val users = setOf(
            KSerUser(name = "Alice", age = 30),
            KSerUser(name = "Bob", age = 25),
        )
        pref.set(users)
        assertEquals(users, pref.get())
    }

    @Test
    fun kserializedSetPreference_observeDefaultValue() = runTest(testDispatcher) {
        val default = setOf(KSerUser(name = "FlowDefault", age = 1))
        val pref = preferenceDatastore.kserializedSet("testKSerSetFlowDefault", default)
        val value = pref.asFlow().first()
        assertEquals(default, value)
    }

    @Test
    fun kserializedSetPreference_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedSet<KSerUser>("testKSerSetFlowSet")
        val users = setOf(KSerUser(name = "Charlie", age = 40))
        pref.set(users)
        val value = pref.asFlow().first()
        assertEquals(users, value)
    }

    @Test
    fun kserializedSetPreference_deleteValue() = runTest(testDispatcher) {
        val default = setOf(KSerUser(name = "Default", age = 0))
        val pref = preferenceDatastore.kserializedSet("testKSerSetDelete", default)
        pref.set(setOf(KSerUser(name = "ToDelete", age = 99)))
        assertEquals(setOf(KSerUser(name = "ToDelete", age = 99)), pref.get())

        pref.delete()
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedSetPreference_updateValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedSet(
            "testKSerSetUpdate",
            setOf(KSerUser(name = "Start", age = 1)),
        )
        pref.update { current -> current + KSerUser(name = "Added", age = 2) }
        assertEquals(
            setOf(KSerUser(name = "Start", age = 1), KSerUser(name = "Added", age = 2)),
            pref.get(),
        )
    }

    @Test
    fun kserializedSetPreference_resetToDefault() = runTest(testDispatcher) {
        val default = setOf(KSerUser(name = "Default", age = 5))
        val pref = preferenceDatastore.kserializedSet("testKSerSetReset", default)
        pref.set(setOf(KSerUser(name = "Changed", age = 99)))
        assertEquals(setOf(KSerUser(name = "Changed", age = 99)), pref.get())

        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedSetPreference_handleCorruptedElementSkipped() = runTest(testDispatcher) {
        val default = setOf<KSerUser>()
        val pref = preferenceDatastore.kserializedSet("testKSerSetCorrupt", default)

        val stringSetKey = stringSetPreferencesKey("testKSerSetCorrupt")
        dataStore.edit { settings ->
            settings[stringSetKey] = setOf(
                """{"name":"Valid","age":10}""",
                "NOT_VALID_JSON",
            )
        }

        val result = pref.get()
        assertEquals(1, result.size)
        assertEquals(KSerUser(name = "Valid", age = 10), result.first())
    }

    @Test
    fun kserializedSetPreference_allCorruptedReturnsEmptySet() = runTest(testDispatcher) {
        val default = setOf<KSerUser>()
        val pref = preferenceDatastore.kserializedSet("testKSerSetAllCorrupt", default)

        val stringSetKey = stringSetPreferencesKey("testKSerSetAllCorrupt")
        dataStore.edit { settings ->
            settings[stringSetKey] = setOf("INVALID_1", "INVALID_2")
        }

        val result = pref.get()
        assertTrue(result.isEmpty())
    }

    @Test
    fun kserializedSetPreference_differentSerializableType() = runTest(testDispatcher) {
        val addresses = setOf(
            KSerAddress(street = "123 Main St", city = "Springfield", zip = "62701"),
            KSerAddress(street = "456 Oak Ave", city = "Shelbyville", zip = "62702"),
        )
        val pref = preferenceDatastore.kserializedSet<KSerAddress>("testKSerSetAddress")
        pref.set(addresses)
        assertEquals(addresses, pref.get())
    }

    @Test
    fun kserializedSetPreference_stateIn() = runTest(testDispatcher) {
        val default = setOf(KSerUser(name = "StateIn", age = 7))
        val pref = preferenceDatastore.kserializedSet("testKSerSetStateIn", default)
        val childScope = this + Job()
        val stateFlow = pref.stateIn(childScope)
        assertEquals(default, stateFlow.value)

        val updated = setOf(KSerUser(name = "NewState", age = 8))
        pref.set(updated)
        val value = stateFlow.first { it.any { u -> u.name == "NewState" } }
        assertEquals(updated, value)
        childScope.cancel()
    }

    @Test
    fun kserializedSetPreference_emptySet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserializedSet<KSerUser>("testKSerSetEmpty")
        pref.set(emptySet())
        assertEquals(emptySet(), pref.get())
    }
}
