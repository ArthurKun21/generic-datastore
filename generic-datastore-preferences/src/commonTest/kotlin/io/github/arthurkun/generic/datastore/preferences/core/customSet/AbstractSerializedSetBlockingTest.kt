package io.github.arthurkun.generic.datastore.preferences.core.data.customSet

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.data.SerializableObject
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractSerializedSetBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    private fun serialize(value: SerializableObject): String = "${value.id}:${value.name}"

    private fun deserialize(value: String): SerializableObject {
        val parts = value.split(":")
        return SerializableObject(parts[0].toInt(), parts[1])
    }

    @Test
    fun serializedSetPreference_defaultValueWhenNotSetBlocking() {
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = "serSetBlockingDefault",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        assertEquals(emptySet(), pref.getBlocking())
    }

    @Test
    fun serializedSetPreference_setAndGetBlocking() {
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = "serSetBlockingSetGet",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        val value = setOf(SerializableObject(1, "Alice"), SerializableObject(2, "Bob"))
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
    }

    @Test
    fun serializedSetPreference_resetToDefaultBlocking() {
        val default = setOf(SerializableObject(4, "Four"))
        val pref = preferenceDatastore.serializedSet(
            key = "serSetBlockingReset",
            defaultValue = default,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.setBlocking(setOf(SerializableObject(1, "One")))
        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun serializedSetPreference_delegation() {
        val default = setOf(SerializableObject(8, "Eight"))
        val pref = preferenceDatastore.serializedSet(
            key = "serSetBlockingDelegate",
            defaultValue = default,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        var delegated: Set<SerializableObject> by pref

        val newValue = setOf(SerializableObject(9, "Nine"))
        delegated = newValue
        assertEquals(newValue, delegated)
        assertEquals(newValue, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, delegated)
    }
}
