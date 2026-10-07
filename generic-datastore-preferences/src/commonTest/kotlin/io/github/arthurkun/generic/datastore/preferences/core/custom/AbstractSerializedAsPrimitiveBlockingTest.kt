package io.github.arthurkun.generic.datastore.preferences.core.custom

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractSerializedAsPrimitiveBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun serializedAsInt_setGetAndResetBlocking() {
        val default = serializedAsValue(0)
        val pref = preferenceDatastore.serializedAsInt(
            key = "serializedAsIntBlocking",
            defaultValue = default,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        pref.setBlocking(SerializedAsValue(3, "ignored"))
        assertEquals(serializedAsValue(3), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, pref.getBlocking())
    }

    @Test
    fun serializedAsInt_delegation() {
        val default = serializedAsValue(0)
        val pref = preferenceDatastore.serializedAsInt(
            key = "serializedAsIntDelegation",
            defaultValue = default,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        var delegated: SerializedAsValue by pref
        delegated = SerializedAsValue(8, "ignored")

        assertEquals(serializedAsValue(8), delegated)
        assertEquals(serializedAsValue(8), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(default, delegated)
    }

    @Test
    fun serializedAsLong_setAndGetBlocking() {
        val pref = preferenceDatastore.serializedAsLong(
            key = "serializedAsLongBlocking",
            defaultValue = serializedAsValue(0),
            serializer = { it.code.toLong() },
            deserializer = { serializedAsValue(it.toInt()) },
        )

        pref.setBlocking(SerializedAsValue(13, "ignored"))
        assertEquals(serializedAsValue(13), pref.getBlocking())
    }

    @Test
    fun serializedAsFloat_setAndGetBlocking() {
        val pref = preferenceDatastore.serializedAsFloat(
            key = "serializedAsFloatBlocking",
            defaultValue = serializedAsValue(0),
            serializer = { it.code.toFloat() },
            deserializer = { serializedAsValue(it.toInt()) },
        )

        pref.setBlocking(SerializedAsValue(23, "ignored"))
        assertEquals(serializedAsValue(23), pref.getBlocking())
    }

    @Test
    fun serializedAsDouble_setAndGetBlocking() {
        val pref = preferenceDatastore.serializedAsDouble(
            key = "serializedAsDoubleBlocking",
            defaultValue = serializedAsValue(0),
            serializer = { it.code.toDouble() },
            deserializer = { serializedAsValue(it.toInt()) },
        )

        pref.setBlocking(SerializedAsValue(33, "ignored"))
        assertEquals(serializedAsValue(33), pref.getBlocking())
    }
}
