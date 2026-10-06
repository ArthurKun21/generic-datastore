package io.github.arthurkun.generic.datastore.preferences.optional.data.customSet

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.data.SerializableObject
import io.github.arthurkun.generic.datastore.preferences.nullableEnum
import io.github.arthurkun.generic.datastore.preferences.nullableEnumSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableSetBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    private fun serialize(value: SerializableObject): String = "${value.id}:${value.name}"

    private fun deserialize(value: String): SerializableObject {
        val parts = value.split(":")
        return SerializableObject(parts[0].toInt(), parts[1])
    }

    @Test
    fun nullableSerializedSet_defaultIsNullBlocking() {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetBlockingDefault",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableSerializedSet_setAndGetBlocking() {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetBlockingSetGet",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        val value = setOf(SerializableObject(1, "Alice"), SerializableObject(2, "Bob"))
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
    }

    @Test
    fun nullableSerializedSet_setNullBlockingClearsValue() {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetBlockingSetNull",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.setBlocking(setOf(SerializableObject(1, "One")))
        pref.setBlocking(null)
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableSerializedSet_resetToDefaultBlocking() {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetBlockingReset",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.setBlocking(setOf(SerializableObject(1, "One")))
        pref.resetToDefaultBlocking()
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableSerializedSet_delegation() {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetBlockingDelegate",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        var delegated: Set<SerializableObject>? by pref

        assertNull(delegated)
        delegated = setOf(SerializableObject(5, "Five"))
        assertEquals(setOf(SerializableObject(5, "Five")), delegated)
        assertEquals(setOf(SerializableObject(5, "Five")), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertNull(delegated)
    }

    @Test
    fun nullableEnum_defaultIsNullBlocking() {
        val pref = preferenceDatastore.nullableEnum<NullableEnumSetTestColor>("nullableEnumBlockingDefault")
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableEnum_setAndGetBlocking() {
        val pref = preferenceDatastore.nullableEnum<NullableEnumSetTestColor>("nullableEnumBlockingSetGet")
        pref.setBlocking(NullableEnumSetTestColor.GREEN)
        assertEquals(NullableEnumSetTestColor.GREEN, pref.getBlocking())
    }

    @Test
    fun nullableEnumSet_setAndGetBlocking() {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetBlockingSetGet")
        val value = setOf(NullableEnumSetTestColor.RED, NullableEnumSetTestColor.BLUE)
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
    }

    @Test
    fun nullableEnumSet_resetToDefaultBlocking() {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetBlockingReset")
        pref.setBlocking(setOf(NullableEnumSetTestColor.RED))
        pref.resetToDefaultBlocking()
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableEnumSet_delegation() {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetBlockingDelegate")
        var delegated: Set<NullableEnumSetTestColor>? by pref

        assertNull(delegated)
        delegated = setOf(NullableEnumSetTestColor.GREEN)
        assertEquals(setOf(NullableEnumSetTestColor.GREEN), delegated)

        pref.resetToDefaultBlocking()
        assertNull(delegated)
    }
}
