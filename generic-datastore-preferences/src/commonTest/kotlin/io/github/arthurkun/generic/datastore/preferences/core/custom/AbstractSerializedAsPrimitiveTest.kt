package io.github.arthurkun.generic.datastore.preferences.core.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Simple value used to exercise the primitive-keyed custom serialization factories. */
data class SerializedAsValue(val code: Int, val label: String)

/** Canonical value the (de)serializers in these tests produce for [code]. */
internal fun serializedAsValue(code: Int): SerializedAsValue = SerializedAsValue(code, "value-$code")

abstract class AbstractSerializedAsPrimitiveTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    private val default = serializedAsValue(0)

    // --- serializedAsInt ---

    @Test
    fun serializedAsInt_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedAsInt(
            key = "serializedAsIntDefault",
            defaultValue = default,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedAsInt_setAndGetStoresUnderIntKey() = runTest(testDispatcher) {
        val key = "serializedAsIntSetGet"
        val pref = preferenceDatastore.serializedAsInt(
            key = key,
            defaultValue = default,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        pref.set(SerializedAsValue(7, "ignored"))

        assertEquals(serializedAsValue(7), pref.get())
        assertEquals(7, dataStore.data.first()[intPreferencesKey(key)])
    }

    @Test
    fun serializedAsInt_decodeFailureReturnsDefault() = runTest(testDispatcher) {
        val key = "serializedAsIntCorrupt"
        val pref = preferenceDatastore.serializedAsInt(
            key = key,
            defaultValue = default,
            serializer = { it.code },
            deserializer = { if (it < 0) error("negative") else serializedAsValue(it) },
        )

        dataStore.edit { it[intPreferencesKey(key)] = -1 }

        assertEquals(default, pref.get())
    }

    @Test
    fun serializedAsInt_flowAndUpdateAndReset() = runTest(testDispatcher) {
        val key = "serializedAsIntLifecycle"
        val pref = preferenceDatastore.serializedAsInt(
            key = key,
            defaultValue = default,
            serializer = { it.code },
            deserializer = { serializedAsValue(it) },
        )

        assertEquals(default, pref.asFlow().first())

        pref.update { it.copy(code = it.code + 5) }
        assertEquals(serializedAsValue(5), pref.get())
        assertEquals(serializedAsValue(5), pref.asFlow().first())

        pref.delete()
        assertEquals(default, pref.get())

        pref.set(SerializedAsValue(9, "ignored"))
        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    // --- serializedAsLong ---

    @Test
    fun serializedAsLong_roundTripStoresUnderLongKey() = runTest(testDispatcher) {
        val key = "serializedAsLongSetGet"
        val pref = preferenceDatastore.serializedAsLong(
            key = key,
            defaultValue = default,
            serializer = { it.code.toLong() + 1_000L },
            deserializer = { serializedAsValue((it - 1_000L).toInt()) },
        )

        pref.set(SerializedAsValue(11, "ignored"))

        assertEquals(serializedAsValue(11), pref.get())
        assertEquals(1_011L, dataStore.data.first()[longPreferencesKey(key)])
    }

    // --- serializedAsFloat ---

    @Test
    fun serializedAsFloat_roundTripStoresUnderFloatKey() = runTest(testDispatcher) {
        val key = "serializedAsFloatSetGet"
        val pref = preferenceDatastore.serializedAsFloat(
            key = key,
            defaultValue = default,
            serializer = { it.code.toFloat() + 0.5f },
            deserializer = { serializedAsValue((it - 0.5f).toInt()) },
        )

        pref.set(SerializedAsValue(21, "ignored"))

        assertEquals(serializedAsValue(21), pref.get())
        assertEquals(21.5f, dataStore.data.first()[floatPreferencesKey(key)])
    }

    // --- serializedAsDouble ---

    @Test
    fun serializedAsDouble_roundTripStoresUnderDoubleKey() = runTest(testDispatcher) {
        val key = "serializedAsDoubleSetGet"
        val pref = preferenceDatastore.serializedAsDouble(
            key = key,
            defaultValue = default,
            serializer = { it.code.toDouble() + 0.25 },
            deserializer = { serializedAsValue((it - 0.25).toInt()) },
        )

        pref.set(SerializedAsValue(31, "ignored"))

        assertEquals(serializedAsValue(31), pref.get())
        assertEquals(31.25, dataStore.data.first()[doublePreferencesKey(key)])
    }

    @Test
    fun serializedAsDouble_decodeFailureReturnsDefault() = runTest(testDispatcher) {
        val key = "serializedAsDoubleCorrupt"
        val pref = preferenceDatastore.serializedAsDouble(
            key = key,
            defaultValue = default,
            serializer = { it.code.toDouble() },
            deserializer = { if (it < 0.0) error("negative") else serializedAsValue(it.toInt()) },
        )

        dataStore.edit { it[doublePreferencesKey(key)] = -1.0 }

        assertEquals(default, pref.get())
    }
}
