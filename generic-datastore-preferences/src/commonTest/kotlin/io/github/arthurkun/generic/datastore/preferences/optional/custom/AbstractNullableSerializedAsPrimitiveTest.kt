package io.github.arthurkun.generic.datastore.preferences.optional.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.custom.SerializedAsValue
import io.github.arthurkun.generic.datastore.preferences.core.custom.serializedAsValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableSerializedAsPrimitiveTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // --- nullableSerializedAsInt ---

    @Test
    fun nullableSerializedAsInt_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = "nullableSerializedAsIntDefault",
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedAsInt_setGetAndStoresUnderIntKey() = runTest(testDispatcher) {
        val key = "nullableSerializedAsIntSetGet"
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = key,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        pref.set(SerializedAsValue(4, "ignored"))

        assertEquals(serializedAsValue(4), pref.get())
        assertEquals(4, dataStore.data.first()[intPreferencesKey(key)])
    }

    @Test
    fun nullableSerializedAsInt_setNullRemovesKey() = runTest(testDispatcher) {
        val key = "nullableSerializedAsIntSetNull"
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = key,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        pref.set(SerializedAsValue(6, "ignored"))
        pref.set(null)

        assertNull(pref.get())
        assertNull(dataStore.data.first()[intPreferencesKey(key)])
    }

    @Test
    fun nullableSerializedAsInt_decodeFailureReturnsNull() = runTest(testDispatcher) {
        val key = "nullableSerializedAsIntCorrupt"
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = key,
            serializer = { it.code },
            deserializer = { if (it < 0) error("negative") else serializedAsValue(it) },
        )

        dataStore.edit { it[intPreferencesKey(key)] = -1 }

        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedAsInt_flowUpdateDeleteAndReset() = runTest(testDispatcher) {
        val key = "nullableSerializedAsIntLifecycle"
        val pref = preferenceDatastore.nullableSerializedAsInt(
            key = key,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        assertNull(pref.asFlow().first())

        pref.update { current -> current ?: SerializedAsValue(1, "ignored") }
        assertEquals(serializedAsValue(1), pref.asFlow().first())

        pref.delete()
        assertNull(pref.get())

        pref.set(SerializedAsValue(2, "ignored"))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    // --- nullableSerializedAsLong ---

    @Test
    fun nullableSerializedAsLong_roundTripStoresUnderLongKey() = runTest(testDispatcher) {
        val key = "nullableSerializedAsLongSetGet"
        val pref = preferenceDatastore.nullableSerializedAsLong(
            key = key,
            serializer = { it.code.toLong() + 1_000L },
            deserializer = { serializedAsValue((it - 1_000L).toInt()) },
        )

        pref.set(SerializedAsValue(12, "ignored"))

        assertEquals(serializedAsValue(12), pref.get())
        assertEquals(1_012L, dataStore.data.first()[longPreferencesKey(key)])

        pref.set(null)
        assertNull(pref.get())
    }

    // --- nullableSerializedAsFloat ---

    @Test
    fun nullableSerializedAsFloat_roundTripStoresUnderFloatKey() = runTest(testDispatcher) {
        val key = "nullableSerializedAsFloatSetGet"
        val pref = preferenceDatastore.nullableSerializedAsFloat(
            key = key,
            serializer = { it.code.toFloat() + 0.5f },
            deserializer = { serializedAsValue((it - 0.5f).toInt()) },
        )

        pref.set(SerializedAsValue(22, "ignored"))

        assertEquals(serializedAsValue(22), pref.get())
        assertEquals(22.5f, dataStore.data.first()[floatPreferencesKey(key)])

        pref.set(null)
        assertNull(pref.get())
    }

    // --- nullableSerializedAsDouble ---

    @Test
    fun nullableSerializedAsDouble_roundTripStoresUnderDoubleKey() = runTest(testDispatcher) {
        val key = "nullableSerializedAsDoubleSetGet"
        val pref = preferenceDatastore.nullableSerializedAsDouble(
            key = key,
            serializer = { it.code.toDouble() + 0.25 },
            deserializer = { serializedAsValue((it - 0.25).toInt()) },
        )

        pref.set(SerializedAsValue(32, "ignored"))

        assertEquals(serializedAsValue(32), pref.get())
        assertEquals(32.25, dataStore.data.first()[doublePreferencesKey(key)])

        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedAsDouble_decodeFailureReturnsNull() = runTest(testDispatcher) {
        val key = "nullableSerializedAsDoubleCorrupt"
        val pref = preferenceDatastore.nullableSerializedAsDouble(
            key = key,
            serializer = { it.code.toDouble() },
            deserializer = { if (it < 0.0) error("negative") else serializedAsValue(it.toInt()) },
        )

        dataStore.edit { it[doublePreferencesKey(key)] = -1.0 }

        assertNull(pref.get())
    }
}
