package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.core.custom.TestItem
import io.github.arthurkun.generic.datastore.proto.nullableKserializedListField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableKserializedListFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableKserializedListField_getBlockingReturnsNull() {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        assertNull(listPref.getBlocking())
    }

    @Test
    fun nullableKserializedListField_setBlockingAndGetBlocking() {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1))
        listPref.setBlocking(items)
        assertEquals(items, listPref.getBlocking())
    }

    @Test
    fun nullableKserializedListField_setBlockingNull() {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.setBlocking(listOf(TestItem("A", 1)))
        listPref.setBlocking(null)
        assertNull(listPref.getBlocking())
    }

    @Test
    fun nullableKserializedListField_resetToDefaultBlocking() {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.setBlocking(listOf(TestItem("A", 1)))
        listPref.resetToDefaultBlocking()
        assertNull(listPref.getBlocking())
    }

    @Test
    fun nullableKserializedListField_propertyDelegation() {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        var items: List<TestItem>? by listPref
        assertNull(items)
        items = listOf(TestItem("Delegated", 7))
        assertEquals(listOf(TestItem("Delegated", 7)), items)
        items = null
        assertNull(items)
    }
}
