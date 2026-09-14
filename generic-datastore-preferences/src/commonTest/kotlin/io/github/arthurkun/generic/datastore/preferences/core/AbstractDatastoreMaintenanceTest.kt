package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.BasePreference
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class AbstractDatastoreMaintenanceTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun keys_returnsOnlyStoredKeys() = runTest(testDispatcher) {
        assertEquals(emptySet(), preferenceDatastore.keys())

        preferenceDatastore.string("keys_string", "default").set("stored")
        preferenceDatastore.int("keys_int", 0).set(1)

        assertEquals(setOf("keys_string", "keys_int"), preferenceDatastore.keys())
    }

    @Test
    fun contains_reflectsStoredState() = runTest(testDispatcher) {
        val pref = preferenceDatastore.string("contains_string", "default")
        assertFalse(preferenceDatastore.contains("contains_string"))

        pref.set("stored")
        assertTrue(preferenceDatastore.contains("contains_string"))

        pref.delete()
        assertFalse(preferenceDatastore.contains("contains_string"))
    }

    @Test
    fun clear_removesOnlyMatchingPrefix() = runTest(testDispatcher) {
        preferenceDatastore.string("session_a", "").set("a")
        preferenceDatastore.string("session_b", "").set("b")
        preferenceDatastore.string("keep_c", "").set("c")

        preferenceDatastore.clear("session_")

        assertFalse(preferenceDatastore.contains("session_a"))
        assertFalse(preferenceDatastore.contains("session_b"))
        assertTrue(preferenceDatastore.contains("keep_c"))
    }

    @Test
    fun clearPrivate_removesOnlyPrivateKeys() = runTest(testDispatcher) {
        val token = preferenceDatastore.string(BasePreference.privateKey("token"), "")
        val regular = preferenceDatastore.string("regular_key", "")
        token.set("secret")
        regular.set("visible")

        preferenceDatastore.clearPrivate()

        assertFalse(preferenceDatastore.contains(BasePreference.privateKey("token")))
        assertTrue(preferenceDatastore.contains("regular_key"))
    }

    @Test
    fun clearAppState_removesOnlyAppStateKeys() = runTest(testDispatcher) {
        val state = preferenceDatastore.int(BasePreference.appStateKey("last_sync"), 0)
        val regular = preferenceDatastore.int("regular_int", 0)
        state.set(42)
        regular.set(7)

        preferenceDatastore.clearAppState()

        assertFalse(preferenceDatastore.contains(BasePreference.appStateKey("last_sync")))
        assertTrue(preferenceDatastore.contains("regular_int"))
    }
}
