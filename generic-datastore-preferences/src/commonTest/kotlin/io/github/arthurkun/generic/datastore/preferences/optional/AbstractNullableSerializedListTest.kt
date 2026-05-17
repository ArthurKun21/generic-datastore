package io.github.arthurkun.generic.datastore.preferences.optional

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.core.KSerUser
import io.github.arthurkun.generic.datastore.preferences.nullableKserializedList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractNullableSerializedListTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    // --- nullableSerializedList tests ---

    @Test
    fun nullableSerializedList_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListDefault",
            serializer = { it },
            deserializer = { it },
        )
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedList_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListSetGet",
            serializer = { it },
            deserializer = { it },
        )
        val items = listOf("hello", "world")
        pref.set(items)
        assertEquals(items, pref.get())
    }

    @Test
    fun nullableSerializedList_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListSetNull",
            serializer = { it },
            deserializer = { it },
        )
        pref.set(listOf("hello"))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedList_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListDelete",
            serializer = { it },
            deserializer = { it },
        )
        pref.set(listOf("toDelete"))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedList_observeDefaultValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListFlowDefault",
            serializer = { it },
            deserializer = { it },
        )
        val value = pref.asFlow().first()
        assertNull(value)
    }

    @Test
    fun nullableSerializedList_updateFromNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListUpdateNull",
            serializer = { it },
            deserializer = { it },
        )
        pref.update { current -> (current ?: emptyList()) + "added" }
        assertEquals(listOf("added"), pref.get())
    }

    @Test
    fun nullableSerializedList_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListResetDefault",
            serializer = { it },
            deserializer = { it },
        )
        pref.set(listOf("value"))
        pref.resetToDefault()
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedList_corruptedDataReturnsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListCorrupt",
            serializer = { it },
            deserializer = { it },
        )
        val stringKey = stringPreferencesKey("nullableSerListCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "NOT_VALID_JSON"
        }
        assertNull(pref.get())
    }

    @Test
    fun nullableSerializedList_preservesOrder() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableSerializedList<String>(
            key = "nullableSerListOrder",
            serializer = { it },
            deserializer = { it },
        )
        val items = listOf("c", "a", "b", "a")
        pref.set(items)
        assertEquals(items, pref.get())
    }

    // --- nullableKserializedList tests ---

    @Test
    fun nullableKserializedList_defaultIsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedList<KSerUser>("nullableKSerListDefault")
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedList_setAndGet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedList<KSerUser>("nullableKSerListSetGet")
        val items = listOf(KSerUser(name = "Alice", age = 30), KSerUser(name = "Bob", age = 25))
        pref.set(items)
        assertEquals(items, pref.get())
    }

    @Test
    fun nullableKserializedList_setNullClearsValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedList<KSerUser>("nullableKSerListSetNull")
        pref.set(listOf(KSerUser(name = "Alice", age = 30)))
        pref.set(null)
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedList_deleteResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedList<KSerUser>("nullableKSerListDelete")
        pref.set(listOf(KSerUser(name = "Alice", age = 30)))
        pref.delete()
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedList_corruptedDataReturnsNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedList<KSerUser>("nullableKSerListCorrupt")
        val stringKey = stringPreferencesKey("nullableKSerListCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "NOT_VALID_JSON"
        }
        assertNull(pref.get())
    }

    @Test
    fun nullableKserializedList_resetToDefaultResetsToNull() = runTest(testDispatcher) {
        val pref = preferenceDatastore.nullableKserializedList<KSerUser>("nullableKSerListResetDefault")
        pref.set(listOf(KSerUser(name = "Alice", age = 30)))
        pref.resetToDefault()
        assertNull(pref.get())
    }
}
