package io.github.arthurkun.generic.datastore.preferences.core.mem

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Blocking (non-suspending) suite for the `*InMemory` preference factories, following the
 * separate blocking-test pattern: only [preferenceDatastore] is required.
 */
abstract class AbstractInMemoryPreferencesBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun stringInMemoryBlocking_getAndSet() {
        val pref = preferenceDatastore.stringInMemory("memBlockingString", "defaultValue")

        assertEquals("defaultValue", pref.getBlocking())

        pref.setBlocking("blockingValue")

        assertEquals("blockingValue", pref.getBlocking())
    }

    @Test
    fun stringInMemoryBlocking_resetToDefault() {
        val pref = preferenceDatastore.stringInMemory("memBlockingStringReset", "defaultValue")

        pref.setBlocking("valueToReset")
        pref.resetToDefaultBlocking()

        assertEquals("defaultValue", pref.getBlocking())
    }

    @Test
    fun stringInMemoryBlocking_propertyDelegation() {
        val pref = preferenceDatastore.stringInMemory("memBlockingDelegate", "initial")

        var delegated by pref

        assertEquals("initial", delegated)

        delegated = "assigned"

        assertEquals("assigned", delegated)
        assertEquals("assigned", pref.getBlocking())
    }

    @Test
    fun intInMemoryBlocking_getAndSet() {
        val pref = preferenceDatastore.intInMemory("memBlockingInt", 0)

        pref.setBlocking(11)

        assertEquals(11, pref.getBlocking())
    }

    @Test
    fun nullableStringInMemoryBlocking_delegationSupportsNull() {
        val pref = preferenceDatastore.nullableStringInMemory("memBlockingNullable")

        var delegated by pref

        assertNull(delegated)

        delegated = "present"

        assertEquals("present", pref.getBlocking())

        pref.setBlocking(null)

        assertNull(pref.getBlocking())
    }
}
