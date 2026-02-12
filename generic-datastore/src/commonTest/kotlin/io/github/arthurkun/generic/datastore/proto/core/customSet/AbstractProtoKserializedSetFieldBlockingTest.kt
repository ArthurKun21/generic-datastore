package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.kserializedSetField
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.core.custom.TestItem
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedSetFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun kserializedSetField_getBlockingReturnsEmptySet() {
        val setPref = protoDatastore.kserializedSetField(
            key = "item_set",
            defaultValue = emptySet<TestItem>(),
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        assertEquals(emptySet(), setPref.getBlocking())
    }

    @Test
    fun kserializedSetField_setBlockingAndGetBlocking() {
        val setPref = protoDatastore.kserializedSetField(
            key = "item_set",
            defaultValue = emptySet<TestItem>(),
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        val items = setOf(TestItem("A", 1), TestItem("B", 2))
        setPref.setBlocking(items)
        assertEquals(items, setPref.getBlocking())
    }

    @Test
    fun kserializedSetField_resetToDefaultBlocking() {
        val setPref = protoDatastore.kserializedSetField(
            key = "item_set",
            defaultValue = emptySet<TestItem>(),
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.setBlocking(setOf(TestItem("A", 1)))
        setPref.resetToDefaultBlocking()
        assertEquals(emptySet(), setPref.getBlocking())
    }

    @Test
    fun kserializedSetField_propertyDelegation() {
        val setPref = protoDatastore.kserializedSetField(
            key = "item_set",
            defaultValue = emptySet<TestItem>(),
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        var items: Set<TestItem> by setPref
        items = setOf(TestItem("Delegated", 7))
        assertEquals(setOf(TestItem("Delegated", 7)), items)
    }
}
