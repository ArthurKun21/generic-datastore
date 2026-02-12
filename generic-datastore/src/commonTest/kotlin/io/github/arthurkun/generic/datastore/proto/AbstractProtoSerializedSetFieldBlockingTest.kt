package io.github.arthurkun.generic.datastore.proto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedSetFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    private val elemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val elemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun serializedSetField_getBlockingReturnsEmptySet() {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        assertEquals(emptySet(), setPref.getBlocking())
    }

    @Test
    fun serializedSetField_setBlockingAndGetBlocking() {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        val items = setOf(TestItem("A", 1), TestItem("B", 2))
        setPref.setBlocking(items)
        assertEquals(items, setPref.getBlocking())
    }

    @Test
    fun serializedSetField_resetToDefaultBlocking() {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.setBlocking(setOf(TestItem("A", 1)))
        setPref.resetToDefaultBlocking()
        assertEquals(emptySet(), setPref.getBlocking())
    }

    @Test
    fun serializedSetField_propertyDelegation() {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        var items: Set<TestItem> by setPref
        items = setOf(TestItem("Delegated", 7))
        assertEquals(setOf(TestItem("Delegated", 7)), items)
    }
}
