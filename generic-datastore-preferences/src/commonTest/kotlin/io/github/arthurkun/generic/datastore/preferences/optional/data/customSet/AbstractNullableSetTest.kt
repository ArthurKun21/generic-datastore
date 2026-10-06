package io.github.arthurkun.generic.datastore.preferences.optional.data.customSet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.data.SerializableObject
import io.github.arthurkun.generic.datastore.preferences.core.data.custom.KSerUser
import io.github.arthurkun.generic.datastore.preferences.enumSet
import io.github.arthurkun.generic.datastore.preferences.kserializedSet
import io.github.arthurkun.generic.datastore.preferences.nullableEnum
import io.github.arthurkun.generic.datastore.preferences.nullableEnumSet
import io.github.arthurkun.generic.datastore.preferences.nullableKserializedSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

enum class NullableEnumSetTestColor { RED, GREEN, BLUE }

abstract class AbstractNullableSetTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    private fun serialize(value: SerializableObject): String = "${value.id}:${value.name}"

    private fun deserialize(value: String): SerializableObject {
        val parts = value.split(":")
        return SerializableObject(parts[0].toInt(), parts[1])
    }

    // --- nullableSerializedSet ---

    @Test
    fun nullableSerializedSet_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetDefault",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        assertNull(pref.get())
        assertNull(pref.defaultValue)
    }

    @Test
    fun nullableSerializedSet_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetSetGet",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        val value = setOf(SerializableObject(1, "Alice"), SerializableObject(2, "Bob"))
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun nullableSerializedSet_writeStoresSerializedElements() = runTest(testDispatcher) {
        val key = "nullableSerSetRawFormat"
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = key,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(7, "Grace")))
        assertEquals(setOf("7:Grace"), dataStore.data.first()[stringSetPreferencesKey(key)])
    }

    @Test
    fun nullableSerializedSet_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetSetNull",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.set(null)
        assertNull(pref.get())
        assertNull(dataStore.data.first()[stringSetPreferencesKey("nullableSerSetSetNull")])
    }

    @Test
    fun nullableSerializedSet_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetDelete",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedSet_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetReset",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedSet_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetUpdateNull",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.update { current -> current ?: setOf(SerializableObject(3, "Three")) }
        assertEquals(setOf(SerializableObject(3, "Three")), pref.get())
    }

    @Test
    fun nullableSerializedSet_updateToNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetUpdateClear",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        pref.set(setOf(SerializableObject(1, "One")))
        pref.update { null }
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedSet_observeValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = "nullableSerSetFlow",
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        assertNull(pref.asFlow().first())
        pref.set(setOf(SerializableObject(4, "Four")))
        assertEquals(setOf(SerializableObject(4, "Four")), pref.asFlow().first())
    }

    @Test
    fun nullableSerializedSet_skipsUndecodableElements() = runTest(testDispatcher) {
        val key = "nullableSerSetCorruptElements"
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
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
    fun nullableSerializedSet_allElementsUndecodableReadsEmptySet() = runTest(testDispatcher) {
        val key = "nullableSerSetAllCorrupt"
        val pref = preferenceDatastore.nullableSerializedSet<SerializableObject>(
            key = key,
            serializer = ::serialize,
            deserializer = ::deserialize,
        )
        dataStore.edit { settings ->
            settings[stringSetPreferencesKey(key)] = setOf("bad", "worse")
        }
        assertEquals(emptySet(), pref.get())
    }

    // --- nullableKserializedSet ---

    @Test
    fun nullableKserializedSet_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>("nullableKSerSetDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedSet_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>("nullableKSerSetSetGet")
        val value = setOf(KSerUser(name = "Alice", age = 30), KSerUser(name = "Bob", age = 25))
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun nullableKserializedSet_writeStoresJsonElements() = runTest(testDispatcher) {
        val key = "nullableKSerSetRawFormat"
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>(key)
        pref.set(setOf(KSerUser(name = "Alice", age = 30)))
        val stored = dataStore.data.first()[stringSetPreferencesKey(key)].orEmpty()
        assertEquals(1, stored.size)
        assertEquals(setOf(KSerUser(name = "Alice", age = 30)), pref.get())
    }

    @Test
    fun nullableKserializedSet_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedSet<KSerUser>("nullableKSerSetSetNull")
        pref.set(setOf(KSerUser(name = "Bob", age = 25)))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedSet_interopsWithNonNullableKserializedSet() = runTest(testDispatcher) {
        val key = "nullableKSerSetInterop"
        val nullablePref = preferenceDatastore.nullableKserializedSet<KSerUser>(key)
        val nonNullablePref = preferenceDatastore.kserializedSet<KSerUser>(key)
        val value = setOf(KSerUser(name = "Carol", age = 41))

        nullablePref.set(value)
        assertEquals(value, nonNullablePref.get())

        nonNullablePref.set(emptySet())
        assertEquals(emptySet(), nullablePref.get())
    }

    // --- nullableEnum ---

    @Test
    fun nullableEnum_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableEnumSetTestColor>("nullableEnumSetDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableEnum_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnum<NullableEnumSetTestColor>("nullableEnumSetSetGet")
        pref.set(NullableEnumSetTestColor.GREEN)
        assertEquals(NullableEnumSetTestColor.GREEN, pref.get())
    }

    @Test
    fun nullableEnum_unknownStoredNameReadsNull() = runTest(testDispatcher) {
        val key = "nullableEnumSetCorrupt"
        val pref = preferenceDatastore.nullableEnum<NullableEnumSetTestColor>(key)
        dataStore.edit { settings ->
            settings[stringPreferencesKey(key)] = "REMOVED_CONSTANT"
        }
        assertNull(pref.get())
    }

    // --- nullableEnumSet ---

    @Test
    fun nullableEnumSet_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetAbsent")
        assertNull(pref.get())
        assertNull(pref.defaultValue)
    }

    @Test
    fun nullableEnumSet_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetSetGet")
        pref.set(setOf(NullableEnumSetTestColor.RED, NullableEnumSetTestColor.BLUE))
        assertEquals(setOf(NullableEnumSetTestColor.RED, NullableEnumSetTestColor.BLUE), pref.get())
    }

    @Test
    fun nullableEnumSet_writeStoresEnumNames() = runTest(testDispatcher) {
        val key = "nullableEnumSetRawFormat"
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>(key)
        pref.set(setOf(NullableEnumSetTestColor.GREEN))
        assertEquals(setOf("GREEN"), dataStore.data.first()[stringSetPreferencesKey(key)])
    }

    @Test
    fun nullableEnumSet_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetSetNull")
        pref.set(setOf(NullableEnumSetTestColor.RED))
        pref.set(null)
        assertNull(pref.get())
        assertNull(dataStore.data.first()[stringSetPreferencesKey("nullableEnumSetSetNull")])
    }

    @Test
    fun nullableEnumSet_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetDelete")
        pref.set(setOf(NullableEnumSetTestColor.BLUE))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableEnumSet_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetReset")
        pref.set(setOf(NullableEnumSetTestColor.BLUE))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableEnumSet_skipsUnknownStoredNames() = runTest(testDispatcher) {
        val key = "nullableEnumSetUnknown"
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>(key)
        dataStore.edit { settings ->
            settings[stringSetPreferencesKey(key)] = setOf("RED", "REMOVED_CONSTANT")
        }
        assertEquals(setOf(NullableEnumSetTestColor.RED), pref.get())
    }

    @Test
    fun nullableEnumSet_allNamesUnknownReadsEmptySet() = runTest(testDispatcher) {
        val key = "nullableEnumSetAllUnknown"
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>(key)
        dataStore.edit { settings ->
            settings[stringSetPreferencesKey(key)] = setOf("REMOVED_A", "REMOVED_B")
        }
        assertEquals(emptySet(), pref.get())
    }

    @Test
    fun nullableEnumSet_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetUpdate")
        pref.update { current -> current ?: setOf(NullableEnumSetTestColor.GREEN) }
        assertEquals(setOf(NullableEnumSetTestColor.GREEN), pref.get())
    }

    @Test
    fun nullableEnumSet_observeValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>("nullableEnumSetFlow")
        assertNull(pref.asFlow().first())
        pref.set(setOf(NullableEnumSetTestColor.RED))
        assertEquals(setOf(NullableEnumSetTestColor.RED), pref.asFlow().first())
    }

    @Test
    fun nullableEnumSet_interopsWithNonNullableEnumSet() = runTest(testDispatcher) {
        val key = "nullableEnumSetInterop"
        val nullablePref = preferenceDatastore.nullableEnumSet<NullableEnumSetTestColor>(key)
        val nonNullablePref = preferenceDatastore.enumSet<NullableEnumSetTestColor>(key)
        val value = setOf(NullableEnumSetTestColor.BLUE)

        nullablePref.set(value)
        assertEquals(value, nonNullablePref.get())

        nonNullablePref.set(emptySet())
        assertEquals(emptySet(), nullablePref.get())
    }
}
