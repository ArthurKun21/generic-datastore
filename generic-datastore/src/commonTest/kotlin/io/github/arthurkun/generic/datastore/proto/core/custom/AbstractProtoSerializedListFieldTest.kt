package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedListFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    private val elemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val elemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun serializedListField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val listPref = protoDatastore.serializedListField(
            key = "ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        assertEquals(emptyList(), listPref.get())
    }

    @Test
    fun serializedListField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val listPref = protoDatastore.serializedListField(
            key = "ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1), TestItem("B", 2))
        listPref.set(items)
        assertEquals(items, listPref.get())
    }

    @Test
    fun serializedListField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val listPref = protoDatastore.serializedListField(
            key = "ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        assertEquals(emptyList(), listPref.asFlow().first())
        listPref.set(listOf(TestItem("C", 3)))
        assertEquals(listOf(TestItem("C", 3)), listPref.asFlow().first())
    }

    @Test
    fun serializedListField_updateAppendsElement() = runTest(testDispatcher) {
        val listPref = protoDatastore.serializedListField(
            key = "ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.update { it + TestItem("B", 2) }
        assertEquals(listOf(TestItem("A", 1), TestItem("B", 2)), listPref.get())
    }

    @Test
    fun serializedListField_deleteResetsToEmptyDefault() = runTest(testDispatcher) {
        val listPref = protoDatastore.serializedListField(
            key = "ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.delete()
        assertEquals(emptyList(), listPref.get())
    }

    @Test
    fun serializedListField_resetToDefault() = runTest(testDispatcher) {
        val listPref = protoDatastore.serializedListField(
            key = "ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.resetToDefault()
        assertEquals(emptyList(), listPref.get())
    }
}
