package io.github.arthurkun.generic.datastore.preferences.optional.custom

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.custom.SerializedAsValue
import io.github.arthurkun.generic.datastore.preferences.core.custom.serializedAsValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableSerializedAsPrimitiveBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun nullableSerializedAsInt_setGetAndResetBlocking() {
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = "nullableSerializedAsIntBlocking",
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        assertNull(pref.getBlocking())

        pref.setBlocking(SerializedAsValue(5, "ignored"))
        assertEquals(serializedAsValue(5), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertNull(pref.getBlocking())
    }

    @Test
    fun nullableSerializedAsInt_delegation() {
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = "nullableSerializedAsIntDelegation",
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        var delegated: SerializedAsValue? by pref
        delegated = SerializedAsValue(9, "ignored")

        assertEquals(serializedAsValue(9), delegated)
        assertEquals(serializedAsValue(9), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertNull(delegated)
    }

    @Test
    fun nullableSerializedAsLong_setAndGetBlocking() {
        val pref = preferenceDatastore.nullableSerializedAsLong(
            key = "nullableSerializedAsLongBlocking",
            serializer = { it.code.toLong() },
            deserializer = { serializedAsValue(it.toInt()) },
        )

        pref.setBlocking(SerializedAsValue(14, "ignored"))
        assertEquals(serializedAsValue(14), pref.getBlocking())
    }

    @Test
    fun nullableSerializedAsFloat_setAndGetBlocking() {
        val pref = preferenceDatastore.nullableSerializedAsFloat(
            key = "nullableSerializedAsFloatBlocking",
            serializer = { it.code.toFloat() },
            deserializer = { serializedAsValue(it.toInt()) },
        )

        pref.setBlocking(SerializedAsValue(24, "ignored"))
        assertEquals(serializedAsValue(24), pref.getBlocking())
    }

    @Test
    fun nullableSerializedAsDouble_setAndGetBlocking() {
        val pref = preferenceDatastore.nullableSerializedAsDouble(
            key = "nullableSerializedAsDoubleBlocking",
            serializer = { it.code.toDouble() },
            deserializer = { serializedAsValue(it.toInt()) },
        )

        pref.setBlocking(SerializedAsValue(34, "ignored"))
        assertEquals(serializedAsValue(34), pref.getBlocking())
    }
}
