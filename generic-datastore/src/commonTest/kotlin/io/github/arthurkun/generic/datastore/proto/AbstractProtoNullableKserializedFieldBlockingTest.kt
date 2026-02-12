package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableKserializedFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableKserializedField_getBlockingReturnsNull() {
        val itemPref = protoDatastore.nullableKserializedField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        assertNull(itemPref.getBlocking())
    }

    @Test
    fun nullableKserializedField_setBlockingAndGetBlocking() {
        val itemPref = protoDatastore.nullableKserializedField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.setBlocking(item)
        assertEquals(item, itemPref.getBlocking())
    }

    @Test
    fun nullableKserializedField_setBlockingNull() {
        val itemPref = protoDatastore.nullableKserializedField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "Widget", quantity = 5))
        itemPref.setBlocking(null)
        assertNull(itemPref.getBlocking())
    }

    @Test
    fun nullableKserializedField_resetToDefaultBlocking() {
        val itemPref = protoDatastore.nullableKserializedField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "Widget", quantity = 5))
        itemPref.resetToDefaultBlocking()
        assertNull(itemPref.getBlocking())
    }

    @Test
    fun nullableKserializedField_propertyDelegation() {
        val itemPref = protoDatastore.nullableKserializedField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item",
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
