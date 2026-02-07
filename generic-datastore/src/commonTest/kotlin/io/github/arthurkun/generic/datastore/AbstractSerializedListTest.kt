package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractSerializedListTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun serializedListPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList<String>(
            key = "testSerListDefault",
            serializer = { it },
            deserializer = { it },
        )
        assertEquals(emptyList(), pref.get())
    }

    @Test
    fun serializedListPreference_defaultValueCustom() = runTest(testDispatcher) {
        val default = listOf("a", "b", "c")
        val pref = preferenceDatastore.serializedList(
            key = "testSerListDefaultCustom",
            defaultValue = default,
            serializer = { it },
            deserializer = { it },
        )
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedListPreference_setAndGetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList<String>(
            key = "testSerListSetGet",
            serializer = { it },
            deserializer = { it },
        )
        val items = listOf("hello", "world")
        pref.set(items)
        assertEquals(items, pref.get())
    }

    @Test
    fun serializedListPreference_preservesOrder() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList<String>(
            key = "testSerListOrder",
            serializer = { it },
            deserializer = { it },
        )
        val items = listOf("c", "a", "b", "a")
        pref.set(items)
        assertEquals(items, pref.get())
    }

    @Test
    fun serializedListPreference_allowsDuplicates() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList<String>(
            key = "testSerListDuplicates",
            serializer = { it },
            deserializer = { it },
        )
        val items = listOf("x", "x", "y", "x")
        pref.set(items)
        assertEquals(items, pref.get())
    }

    @Test
    fun serializedListPreference_observeDefaultValue() = runTest(testDispatcher) {
        val default = listOf("flow")
        val pref = preferenceDatastore.serializedList(
            key = "testSerListFlowDefault",
            defaultValue = default,
            serializer = { it },
            deserializer = { it },
        )
        val value = pref.asFlow().first()
        assertEquals(default, value)
    }

    @Test
    fun serializedListPreference_observeSetValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList<String>(
            key = "testSerListFlowSet",
            serializer = { it },
            deserializer = { it },
        )
        val items = listOf("observed")
        pref.set(items)
        val value = pref.asFlow().first()
        assertEquals(items, value)
    }

    @Test
    fun serializedListPreference_deleteValue() = runTest(testDispatcher) {
        val default = listOf("default")
        val pref = preferenceDatastore.serializedList(
            key = "testSerListDelete",
            defaultValue = default,
            serializer = { it },
            deserializer = { it },
        )
        pref.set(listOf("toDelete"))
        assertEquals(listOf("toDelete"), pref.get())

        pref.delete()
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedListPreference_updateValue() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList(
            key = "testSerListUpdate",
            defaultValue = listOf("start"),
            serializer = { it },
            deserializer = { it },
        )
        pref.update { current -> current + "added" }
        assertEquals(listOf("start", "added"), pref.get())
    }

    @Test
    fun serializedListPreference_resetToDefault() = runTest(testDispatcher) {
        val default = listOf("default")
        val pref = preferenceDatastore.serializedList(
            key = "testSerListReset",
            defaultValue = default,
            serializer = { it },
            deserializer = { it },
        )
        pref.set(listOf("changed"))
        assertEquals(listOf("changed"), pref.get())

        pref.resetToDefault()
        assertEquals(default, pref.get())
    }

    @Test
    fun serializedListPreference_handleCorruptedDataReturnsDefault() = runTest(testDispatcher) {
        val default = listOf("safe")
        val pref = preferenceDatastore.serializedList(
            key = "testSerListCorrupt",
            defaultValue = default,
            serializer = { it },
            deserializer = { it },
        )

        val stringKey = stringPreferencesKey("testSerListCorrupt")
        dataStore.edit { settings ->
            settings[stringKey] = "NOT_VALID_JSON"
        }

        assertEquals(default, pref.get())
    }

    @Test
    fun serializedListPreference_handleCorruptedElementSkipped() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList(
            key = "testSerListCorruptElement",
            serializer = { it },
            deserializer = { value ->
                if (value == "BAD") throw IllegalArgumentException("bad")
                value
            },
        )

        val stringKey = stringPreferencesKey("testSerListCorruptElement")
        dataStore.edit { settings ->
            settings[stringKey] = """["good","BAD","also_good"]"""
        }

        val result = pref.get()
        assertEquals(listOf("good", "also_good"), result)
    }

    @Test
    fun serializedListPreference_stateIn() = runTest(testDispatcher) {
        val default = listOf("stateIn")
        val pref = preferenceDatastore.serializedList(
            key = "testSerListStateIn",
            defaultValue = default,
            serializer = { it },
            deserializer = { it },
        )
        val childScope = this + Job()
        val stateFlow = pref.stateIn(childScope)
        assertEquals(default, stateFlow.value)

        val updated = listOf("newState")
        pref.set(updated)
        val value = stateFlow.first { it.contains("newState") }
        assertEquals(updated, value)
        childScope.cancel()
    }

    @Test
    fun serializedListPreference_emptyList() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList<String>(
            key = "testSerListEmpty",
            serializer = { it },
            deserializer = { it },
        )
        pref.set(emptyList())
        assertEquals(emptyList(), pref.get())
    }

    @Test
    fun serializedListPreference_customObjectType() = runTest(testDispatcher) {
        val pref = preferenceDatastore.serializedList(
            key = "testSerListCustomObj",
            serializer = { "${it.name}:${it.age}" },
            deserializer = { s ->
                val parts = s.split(":")
                KSerUser(name = parts[0], age = parts[1].toInt())
            },
        )
        val items = listOf(
            KSerUser(name = "Alice", age = 30),
            KSerUser(name = "Bob", age = 25),
        )
        pref.set(items)
        assertEquals(items, pref.get())
    }
}
