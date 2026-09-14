package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchPref
import io.github.arthurkun.generic.datastore.preferences.nullableEnumSet
import io.github.arthurkun.generic.datastore.preferences.nullableKserializedSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private enum class NullableSetTestEnum { ALPHA, BETA }

abstract class AbstractNullableCustomSetTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableSerializedSet_nullWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet(
            "testNullableSet",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedSet_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet(
            "testNullableSet",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )
        pref.set(setOf("a", "b"))
        assertEquals(setOf("a", "b"), pref.get())
    }

    @Test
    fun nullableSerializedSet_setNullRemovesKey() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet(
            "testNullableSet",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )
        pref.set(setOf("a"))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedSet_skipsUndecodableElements() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet(
            "testNullableSetSkip",
            serializer = { it.toString() },
            deserializer = { it.toInt() },
        )
        pref.set(setOf(1, 2))
        dataStore.edit { prefs ->
            prefs[stringSetPreferencesKey("testNullableSetSkip")] = setOf("3", "not-a-number")
        }
        assertEquals(setOf(3), pref.get())
    }

    @Test
    fun nullableSerializedSet_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet(
            "testNullableSetDelete",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )
        pref.set(setOf("a"))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedSet_nullWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>("testNullableKSerSet")
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedSet_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>("testNullableKSerSet")
        val users = setOf(KSerUser(name = "Alice", age = 30), KSerUser(name = "Bob", age = 25))
        pref.set(users)
        assertEquals(users, pref.get())
    }

    @Test
    fun nullableKserializedSet_setNullRemovesKey() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>("testNullableKSerSet")
        pref.set(setOf(KSerUser(name = "Alice", age = 30)))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedSet_batchRoundTrip() = runTest(testDispatcher) {
        var handle: BatchPref<Set<KSerUser>?>? = null
        val users = setOf(KSerUser(name = "Alice", age = 30))

        preferenceDatastore.batchWrite {
            handle = nullableKserializedSet<KSerUser>("testNullableKSerSetBatch")
            set(requireNotNull(handle), users)
        }

        val values = preferenceDatastore.batchReadValues {
            handle = nullableKserializedSet<KSerUser>("testNullableKSerSetBatch")
        }
        assertEquals(users, values[requireNotNull(handle)])
    }

    @Test
    fun nullableEnumSet_nullWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableSetTestEnum>("testNullableEnumSet")
        assertNull(pref.get())
    }

    @Test
    fun nullableEnumSet_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableSetTestEnum>("testNullableEnumSet")
        pref.set(setOf(NullableSetTestEnum.ALPHA, NullableSetTestEnum.BETA))
        assertEquals(
            setOf(NullableSetTestEnum.ALPHA, NullableSetTestEnum.BETA),
            pref.get(),
        )
    }

    @Test
    fun nullableEnumSet_setNullRemovesKey() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableSetTestEnum>("testNullableEnumSet")
        pref.set(setOf(NullableSetTestEnum.ALPHA))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableEnumSet_skipsUnknownNames() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableSetTestEnum>("testNullableEnumSetSkip")
        pref.set(setOf(NullableSetTestEnum.ALPHA))
        dataStore.edit { prefs ->
            prefs[stringSetPreferencesKey("testNullableEnumSetSkip")] = setOf("ALPHA", "GAMMA")
        }
        assertEquals(setOf(NullableSetTestEnum.ALPHA), pref.get())
    }

    @Test
    fun nullableEnumSet_batchRoundTrip() = runTest(testDispatcher) {
        var handle: BatchPref<Set<NullableSetTestEnum>?>? = null

        preferenceDatastore.batchWrite {
            handle = nullableEnumSet<NullableSetTestEnum>("testNullableEnumSetBatch")
            set(requireNotNull(handle), setOf(NullableSetTestEnum.BETA))
        }

        val values = preferenceDatastore.batchReadValues {
            handle = nullableEnumSet<NullableSetTestEnum>("testNullableEnumSetBatch")
        }
        assertEquals(setOf(NullableSetTestEnum.BETA), values[requireNotNull(handle)])
    }

    @Test
    fun nullableSerializedSet_observeValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet(
            "testNullableSetFlow",
            serializer = { it.uppercase() },
            deserializer = { it.lowercase() },
        )
        pref.set(setOf("a"))
        assertEquals(setOf("a"), pref.asFlow().first())
    }
}
