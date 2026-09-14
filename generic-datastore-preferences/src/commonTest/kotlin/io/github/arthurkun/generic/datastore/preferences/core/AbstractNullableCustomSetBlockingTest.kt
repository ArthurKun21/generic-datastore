package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.nullableKserializedSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableCustomSetBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun nullableKserializedSet_blockingRoundTrip() {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>(
            "testNullableKSerSetBlocking",
        )
        assertNull(pref.getBlocking())

        val users = setOf(KSerUser(name = "Alice", age = 30))
        pref.setBlocking(users)
        assertEquals(users, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableKserializedSet_propertyDelegation() {
        var delegated by preferenceDatastore.nullableKserializedSet<KSerUser>(
            "testNullableKSerSetDelegated",
        )
        assertNull(delegated)

        delegated = setOf(KSerUser(name = "Bob", age = 25))
        assertEquals(setOf(KSerUser(name = "Bob", age = 25)), delegated)
    }
}
