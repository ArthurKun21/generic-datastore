package io.github.arthurkun.generic.datastore.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InMemoryGenericPreferenceDatastoreTest {

    @Test
    fun data_startsEmptyByDefault() = runTest {
        val store = InMemoryGenericPreferenceDatastore()

        assertTrue(store.data.first().asMap().isEmpty())
    }

    @Test
    fun constructor_seedsInitialPreferences() = runTest {
        val key = stringPreferencesKey("seeded")
        val initial = emptyPreferences().toMutablePreferences()
            .apply { this[key] = "initial" }
            .toPreferences()
        val store = InMemoryGenericPreferenceDatastore(initial)

        assertEquals("initial", store.data.first()[key])
    }

    @Test
    fun updateData_appliesTransform() = runTest {
        val store = InMemoryGenericPreferenceDatastore()
        val key = stringPreferencesKey("greeting")

        store.updateData { prefs ->
            prefs.toMutablePreferences().apply { this[key] = "hello" }.toPreferences()
        }

        assertEquals("hello", store.data.first()[key])
    }

    @Test
    fun edit_extensionWritesThroughUpdateData() = runTest {
        val store = InMemoryGenericPreferenceDatastore()
        val key = stringPreferencesKey("viaEdit")

        store.edit { it[key] = "value" }

        assertEquals("value", store.data.first()[key])
    }

    @Test
    fun updateData_serializesConcurrentUpdates() = runTest {
        val store = InMemoryGenericPreferenceDatastore()
        val key = intPreferencesKey("counter")

        coroutineScope {
            repeat(50) {
                launch {
                    store.updateData { prefs ->
                        prefs.toMutablePreferences()
                            .apply { this[key] = (prefs[key] ?: 0) + 1 }
                            .toPreferences()
                    }
                }
            }
        }

        assertEquals(50, store.data.first()[key])
    }

    @Test
    fun updateData_throwingTransformLeavesStateUnchanged() = runTest {
        val store = InMemoryGenericPreferenceDatastore()
        val key = stringPreferencesKey("stable")

        store.updateData { prefs ->
            prefs.toMutablePreferences().apply { this[key] = "before" }.toPreferences()
        }

        assertFailsWith<IllegalStateException> {
            store.updateData {
                throw IllegalStateException("transform failed")
            }
        }

        assertEquals("before", store.data.first()[key])
    }

    @Test
    fun data_emitsEveryDistinctUpdate() = runTest {
        val store = InMemoryGenericPreferenceDatastore()
        val key = stringPreferencesKey("stream")
        val emissions = mutableListOf<String?>()

        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            store.data.take(3).collect { emissions.add(it[key]) }
        }

        store.updateData { prefs ->
            prefs.toMutablePreferences().apply { this[key] = "one" }.toPreferences()
        }
        store.updateData { prefs ->
            prefs.toMutablePreferences().apply { this[key] = "two" }.toPreferences()
        }

        collector.join()
        assertEquals(listOf(null, "one", "two"), emissions)
    }
}
