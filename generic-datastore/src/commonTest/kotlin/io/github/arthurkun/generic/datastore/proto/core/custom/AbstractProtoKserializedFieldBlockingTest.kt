package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.kserializedField
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun kserializedField_getBlockingReturnsDefault() {
        val itemPref = protoDatastore.kserializedField(
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.getBlocking())
    }

    @Test
    fun kserializedField_setBlockingAndGetBlocking() {
        val itemPref = protoDatastore.kserializedField(
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.setBlocking(item)
        assertEquals(item, itemPref.getBlocking())
    }

    @Test
    fun kserializedField_resetToDefaultBlocking() {
        val itemPref = protoDatastore.kserializedField(
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.setBlocking(TestItem(name = "ToReset", quantity = 1))
        itemPref.resetToDefaultBlocking()
        assertEquals(TestItem(), itemPref.getBlocking())
    }

    @Test
    fun kserializedField_propertyDelegation() {
        val itemPref = protoDatastore.kserializedField(
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        var item: TestItem by itemPref
        item = TestItem(name = "Delegated", quantity = 7)
        assertEquals(TestItem(name = "Delegated", quantity = 7), item)
    }
}
