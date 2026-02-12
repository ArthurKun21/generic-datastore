package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    private val itemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val itemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun serializedField_getBlockingReturnsDefault() {
        val itemPref = protoDatastore.serializedField(
            defaultValue = TestItem(),
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.getBlocking())
    }

    @Test
    fun serializedField_setBlockingAndGetBlocking() {
        val itemPref = protoDatastore.serializedField(
            defaultValue = TestItem(),
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.setBlocking(item)
        assertEquals(item, itemPref.getBlocking())
    }

    @Test
    fun serializedField_resetToDefaultBlocking() {
        val itemPref = protoDatastore.serializedField(
            defaultValue = TestItem(),
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "ToReset", quantity = 1))
        itemPref.resetToDefaultBlocking()
        assertEquals(TestItem(), itemPref.getBlocking())
    }

    @Test
    fun serializedField_propertyDelegation() {
        val itemPref = protoDatastore.serializedField(
            defaultValue = TestItem(),
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        var item: TestItem by itemPref
        item = TestItem(name = "Delegated", quantity = 7)
        assertEquals(TestItem(name = "Delegated", quantity = 7), item)
    }
}
