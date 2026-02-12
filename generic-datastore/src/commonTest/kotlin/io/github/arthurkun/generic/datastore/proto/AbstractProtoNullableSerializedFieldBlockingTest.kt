package io.github.arthurkun.generic.datastore.proto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableSerializedFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    private val itemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val itemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun nullableSerializedField_getBlockingReturnsNull() {
        val itemPref = protoDatastore.nullableSerializedField(
            key = "nullable_ser_item",
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        assertNull(itemPref.getBlocking())
    }

    @Test
    fun nullableSerializedField_setBlockingAndGetBlocking() {
        val itemPref = protoDatastore.nullableSerializedField(
            key = "nullable_ser_item",
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.setBlocking(item)
        assertEquals(item, itemPref.getBlocking())
    }

    @Test
    fun nullableSerializedField_setBlockingNull() {
        val itemPref = protoDatastore.nullableSerializedField(
            key = "nullable_ser_item",
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "Widget", quantity = 5))
        itemPref.setBlocking(null)
        assertNull(itemPref.getBlocking())
    }

    @Test
    fun nullableSerializedField_resetToDefaultBlocking() {
        val itemPref = protoDatastore.nullableSerializedField(
            key = "nullable_ser_item",
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "Widget", quantity = 5))
        itemPref.resetToDefaultBlocking()
        assertNull(itemPref.getBlocking())
    }

    @Test
    fun nullableSerializedField_propertyDelegation() {
        val itemPref = protoDatastore.nullableSerializedField(
            key = "nullable_ser_item",
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        var item: TestItem? by itemPref
        assertNull(item)
        item = TestItem(name = "Delegated", quantity = 7)
        assertEquals(TestItem(name = "Delegated", quantity = 7), item)
        item = null
        assertNull(item)
    }
}
