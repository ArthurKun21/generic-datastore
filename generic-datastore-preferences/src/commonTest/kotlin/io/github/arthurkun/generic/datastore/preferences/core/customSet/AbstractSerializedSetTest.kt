package io.github.arthurkun.generic.datastore.preferences.core.data.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.data.SerializableObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractSerializedSetTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    private fun serialize(value: SerializableObject): String = "${value.id}:${value.name}"

    private fun deserialize(value: String): SerializableObject {
        val parts = value.split(":")
        return SerializableObject(parts[0].toInt(), parts[1])
    }

    @Test
    fun serializedSetPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = "serSetDefault",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        assertEquals(emptySet(), pref.get())
    }

    @Test
    fun serializedSetPreference_defaultValueCustom() = runTest(testDispatcher) {
        val default = setOf(SerializableObject(1, "Default"))
        val pref = preferenceDatastore.serializedSet(
            key = "serSetDefaultCustom",
            defaultValue = default,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedSetPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = "serSetSetGet",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        val value = setOf(SerializableObject(1, "Alice"), SerializableObject(2, "Bob"))
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun serializedSetPreference_writeStoresSerializedElements() = runTest(testDispatcher) {
        val key = "serSetRawFormat"
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = key,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(7, "Grace")))
        val stored = dataStore.data.first()[stringSetPreferencesKey(key)]
        assertEquals(setOf("7:Grace"), stored)
    }

    @Test
    fun serializedSetPreference_observeValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = "serSetFlow",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(3, "Flow")))
        assertEquals(setOf(SerializableObject(3, "Flow")), pref.asFlow().first())
    }

    @Test
    fun serializedSetPreference_update() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = "serSetUpdate",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.update { current -> current + SerializableObject(2, "Two") }
        assertEquals(
            setOf(SerializableObject(1, "One"), SerializableObject(2, "Two")),
            pref.get(),
        )
    }

    @Test
    fun serializedSetPreference_updateFromDefaultWhenAbsent() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedSet(
            key = "serSetUpdateAbsent",
            defaultValue = setOf(SerializableObject(9, "Default")),
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.update { it + SerializableObject(10, "Added") }
        assertEquals(
            setOf(SerializableObject(9, "Default"), SerializableObject(10, "Added")),
            pref.get(),
        )
    }

    @Test
    fun serializedSetPreference_deleteResetsToDefault() = runTest(testDispatcher) {
        val default = setOf(SerializableObject(0, "Zero"))
        val pref = preferenceDatastore.serializedSet(
            key = "serSetDelete",
            defaultValue = default,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.delete()
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedSetPreference_resetToDefault() = runTest(testDispatcher) {
        val default = setOf(SerializableObject(5, "Five"))
        val pref = preferenceDatastore.serializedSet(
            key = "serSetReset",
            defaultValue = default,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedSetPreference_skipsUndecodableElements() = runTest(testDispatcher) {
        val key = "serSetCorruptElements"
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = key,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        dataStore.edit { settings ->
            settings[stringSetPreferencesKey(key)] = setOf("1:Valid", "not-an-object")
        }
        assertEquals(setOf(SerializableObject(1, "Valid")), pref.get())
    }

    @Test
    fun serializedSetPreference_allElementsUndecodableReadsEmptySet() = runTest(testDispatcher) {
        val key = "serSetAllCorrupt"
        val pref = preferenceDatastore.serializedSet<SerializableObject>(
            key = key,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        dataStore.edit { settings ->
            settings[stringSetPreferencesKey(key)] = setOf("bad", "worse")
        }
        assertEquals(emptySet(), pref.get())
    }
}
