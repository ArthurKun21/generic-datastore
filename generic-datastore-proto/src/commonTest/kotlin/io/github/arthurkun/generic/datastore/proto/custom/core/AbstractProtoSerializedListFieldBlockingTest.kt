package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedListFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    private val elemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val elemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun serializedListField_getBlockingReturnsEmptyList() {
        val listPref = protoDatastore.serializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        assertEquals(emptyList(), listPref.getBlocking())
    }

    @Test
    fun serializedListField_setBlockingAndGetBlocking() {
        val listPref = protoDatastore.serializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1), TestItem("B", 2))
        listPref.setBlocking(items)
        assertEquals(items, listPref.getBlocking())
    }

    @Test
    fun serializedListField_resetToDefaultBlocking() {
        val listPref = protoDatastore.serializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.setBlocking(listOf(TestItem("A", 1)))
        listPref.resetToDefaultBlocking()
        assertEquals(emptyList(), listPref.getBlocking())
    }

    @Test
    fun serializedListField_propertyDelegation() {
        val listPref = protoDatastore.serializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        var items: List<TestItem> by listPref
        items = listOf(TestItem("Delegated", 7))
        assertEquals(listOf(TestItem("Delegated", 7)), items)
    }
}
