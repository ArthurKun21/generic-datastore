package io.github.arthurkun.generic.datastore.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractKSerializedBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun kserializedPreference_resetToDefaultBlocking() {
        val default = KSerUser(name = "BlockingDefault", age = 3)
        val pref = preferenceDatastore.kserialized("testKSerResetBlocking", default)
        pref.setBlocking(KSerUser(name = "BlockingChanged", age = 77))
        assertEquals(KSerUser(name = "BlockingChanged", age = 77), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun kserializedPreference_delegation() {
        val default = KSerUser(name = "DelegateDefault", age = 10)
        val pref = preferenceDatastore.kserialized("testKSerDelegate", default)
        var delegated: KSerUser by pref

        val newUser = KSerUser(name = "Delegated", age = 20)
        delegated = newUser
        assertEquals(newUser, delegated)
        assertEquals(newUser, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, delegated)
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun kserializedPreference_setAndGetBlocking() {
        val pref = preferenceDatastore.kserialized("testKSerBlockingSetGet", KSerUser())
        val user = KSerUser(name = "BlockingUser", age = 42)
        pref.setBlocking(user)
        assertEquals(user, pref.getBlocking())
    }

    @Test
    fun kserializedPreference_resetToDefaultBlocking_whenNeverSet() {
        val default = KSerUser(name = "NeverSet", age = 0)
        val pref = preferenceDatastore.kserialized("testKSerResetNeverSet", default)
        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun kserializedPreference_differentType_blocking() {
        val default = KSerAddress(street = "100 Elm St", city = "Metro", zip = "10001")
        val pref = preferenceDatastore.kserialized("testKSerAddressBlocking", default)
        val updated = KSerAddress(street = "200 Pine Rd", city = "Suburbia", zip = "20002")
        pref.setBlocking(updated)
        assertEquals(updated, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }
}
