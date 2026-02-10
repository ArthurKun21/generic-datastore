package io.github.arthurkun.generic.datastore.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractKSerializedSetBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun kserializedSetPreference_setAndGetBlocking() {
        val pref = preferenceDatastore.kserializedSet<KSerUser>("testKSerSetBlockingSetGet")
        val users = setOf(KSerUser(name = "BlockingUser", age = 42))
        pref.setBlocking(users)
        assertEquals(users, pref.getBlocking())
    }

    @Test
    fun kserializedSetPreference_resetToDefaultBlocking() {
        val default = setOf(KSerUser(name = "BlockingDefault", age = 3))
        val pref = preferenceDatastore.kserializedSet("testKSerSetResetBlocking", default)
        pref.setBlocking(setOf(KSerUser(name = "BlockingChanged", age = 77)))
        assertEquals(setOf(KSerUser(name = "BlockingChanged", age = 77)), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun kserializedSetPreference_delegation() {
        val default = setOf(KSerUser(name = "DelegateDefault", age = 10))
        val pref = preferenceDatastore.kserializedSet("testKSerSetDelegate", default)
        var delegated: Set<KSerUser> by pref

        val newUsers = setOf(KSerUser(name = "Delegated", age = 20))
        delegated = newUsers
        assertEquals(newUsers, delegated)
        assertEquals(newUsers, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, delegated)
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun kserializedSetPreference_resetToDefaultBlocking_whenNeverSet() {
        val default = setOf(KSerUser(name = "NeverSet", age = 0))
        val pref = preferenceDatastore.kserializedSet("testKSerSetResetNeverSet", default)
        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun kserializedSetPreference_differentType_blocking() {
        val default = setOf(KSerAddress(street = "100 Elm St", city = "Metro", zip = "10001"))
        val pref = preferenceDatastore.kserializedSet("testKSerSetAddressBlocking", default)
        val updated = setOf(KSerAddress(street = "200 Pine Rd", city = "Suburbia", zip = "20002"))
        pref.setBlocking(updated)
        assertEquals(updated, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }
}
