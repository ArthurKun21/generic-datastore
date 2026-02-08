package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.kserialized
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class KSerUser(
    val name: String = "",
    val age: Int = 0,
)

@Serializable
data class KSerAddress(
    val street: String = "",
    val city: String = "",
    val zip: String = "",
)

abstract class AbstractKSerializedTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserialized("testKSerDefault", KSerUser())
        assertEquals(KSerUser(), pref.get())
    }

    @Test
    fun kserializedPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserialized("testKSerSetGet", KSerUser())
        val user = KSerUser(name = "Alice", age = 30)
        pref.set(user)
        assertEquals(user, pref.get())
    }

    @Test
    fun kserializedPreference_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserialized("testKSerFlowDefault", KSerUser(name = "Bob", age = 25))
        val value = pref.asFlow().first()
        assertEquals(KSerUser(name = "Bob", age = 25), value)
    }

    @Test
    fun kserializedPreference_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserialized("testKSerFlowSet", KSerUser())
        val user = KSerUser(name = "Charlie", age = 40)
        pref.set(user)
        val value = pref.asFlow().first()
        assertEquals(user, value)
    }

    @Test
    fun kserializedPreference_deleteValue() = runTest(testDispatcher) {
        val default = KSerUser(name = "Default", age = 0)
        val pref = preferenceDatastore.kserialized("testKSerDelete", default)
        pref.set(KSerUser(name = "ToDelete", age = 99))
        assertEquals(KSerUser(name = "ToDelete", age = 99), pref.get())

        pref.delete()
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedPreference_updateValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.kserialized("testKSerUpdate", KSerUser(name = "Start", age = 1))
        pref.update { it.copy(name = "Updated", age = it.age + 10) }
        assertEquals(KSerUser(name = "Updated", age = 11), pref.get())
    }

    @Test
    fun kserializedPreference_resetToDefault() = runTest(testDispatcher) {
        val default = KSerUser(name = "Default", age = 5)
        val pref = preferenceDatastore.kserialized("testKSerReset", default)
        pref.set(KSerUser(name = "Changed", age = 99))
        assertEquals(KSerUser(name = "Changed", age = 99), pref.get())

        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedPreference_handleCorruptedDataReturnsDefault() = runTest(testDispatcher) {
        val default = KSerUser(name = "Safe", age = 0)
        val pref = preferenceDatastore.kserialized("testKSerCorrupt", default)

        val stringKey = stringPreferencesKey("testKSerCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "NOT_VALID_JSON"
        }

        assertEquals(default, pref.get())
    }

    @Test
    fun kserializedPreference_differentSerializableType() = runTest(testDispatcher) {
        val default = KSerAddress(street = "123 Main St", city = "Springfield", zip = "62701")
        val pref = preferenceDatastore.kserialized("testKSerAddress", default)

        val updated = KSerAddress(street = "456 Oak Ave", city = "Shelbyville", zip = "62702")
        pref.set(updated)
        assertEquals(updated, pref.get())
    }

    @Test
    fun kserializedPreference_stateIn() = runTest(testDispatcher) {
        val default = KSerUser(name = "StateIn", age = 7)
        val pref = preferenceDatastore.kserialized("testKSerStateIn", default)
        val childScope = this + Job()
        val stateFlow = pref.stateIn(childScope)
        assertEquals(default, stateFlow.value)

        pref.set(KSerUser(name = "NewState", age = 8))
        val value = stateFlow.first { it.name == "NewState" }
        assertEquals(KSerUser(name = "NewState", age = 8), value)
        childScope.cancel()
    }

    @Test
    fun kserializedPreference_ignoresUnknownKeys() = runTest(testDispatcher) {
        val default = KSerUser()
        val pref = preferenceDatastore.kserialized("testKSerUnknownKeys", default)

        val stringKey = stringPreferencesKey("testKSerUnknownKeys")
        dataStore.edit { settings ->
            settings[stringKey] = """{"name":"Test","age":5,"unknownField":"value"}"""
        }

        assertEquals(KSerUser(name = "Test", age = 5), pref.get())
    }
}
