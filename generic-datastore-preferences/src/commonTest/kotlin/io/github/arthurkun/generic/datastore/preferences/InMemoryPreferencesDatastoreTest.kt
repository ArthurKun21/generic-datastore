package io.github.arthurkun.generic.datastore.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryPreferencesDatastoreTest {

    @Test
    fun inMemory_getSetRoundTrip() = runTest {
        val datastore = createInMemoryPreferencesDatastore()
        val name = datastore.string("name", "")

        assertEquals("", name.get())
        name.set("Alice")
        assertEquals("Alice", name.get())
    }

    @Test
    fun inMemory_stateIsPrivateToInstance() = runTest {
        val first = createInMemoryPreferencesDatastore()
        val second = createInMemoryPreferencesDatastore()

        first.string("shared_key", "").set("only-in-first")

        assertEquals("only-in-first", first.string("shared_key", "").get())
        assertEquals("", second.string("shared_key", "").get())
    }

    @Test
    fun inMemory_supportsFlowAndBatch() = runTest {
        val datastore = createInMemoryPreferencesDatastore()
        val counter = datastore.int("counter", 0)

        counter.set(1)
        assertEquals(1, counter.asFlow().first())

        datastore.batchWrite {
            val a = int("counter", 0)
            val b = string("label", "")
            set(a, 42)
            set(b, "batched")
        }

        assertEquals(42, counter.get())
        assertEquals("batched", datastore.string("label", "").get())
    }

    @Test
    fun inMemory_supportsMaintenanceApis() = runTest {
        val datastore = createInMemoryPreferencesDatastore()
        datastore.string("gone", "").set("value")

        assertTrue(datastore.contains("gone"))
        datastore.clearAll()
        assertFalse(datastore.contains("gone"))
    }

    @Test
    fun inMemory_resetToDefaultAfterWrite() = runTest {
        val datastore = createInMemoryPreferencesDatastore()
        val pref = datastore.string("resettable", "default")

        pref.set("changed")
        pref.resetToDefault()
        assertEquals("default", pref.get())
    }
}
