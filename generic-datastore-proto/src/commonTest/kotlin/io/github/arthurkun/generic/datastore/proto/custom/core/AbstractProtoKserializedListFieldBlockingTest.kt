package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.kserializedListField
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedListFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun kserializedListField_getBlockingReturnsEmptyList() {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        assertEquals(emptyList(), listPref.getBlocking())
    }

    @Test
    fun kserializedListField_setBlockingAndGetBlocking() {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1), TestItem("B", 2))
        listPref.setBlocking(items)
        assertEquals(items, listPref.getBlocking())
    }

    @Test
    fun kserializedListField_resetToDefaultBlocking() {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.setBlocking(listOf(TestItem("A", 1)))
        listPref.resetToDefaultBlocking()
        assertEquals(emptyList(), listPref.getBlocking())
    }

    @Test
    fun kserializedListField_propertyDelegation() {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        var items: List<TestItem> by listPref
        items = listOf(TestItem("Delegated", 7))
        assertEquals(listOf(TestItem("Delegated", 7)), items)
    }
}
