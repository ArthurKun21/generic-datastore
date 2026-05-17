package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.custom.core.TestItem
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableSerializedListFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    private val elemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val elemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun nullableSerializedListField_getBlockingReturnsNull() {
        val listPref = protoDatastore.nullableSerializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        assertNull(listPref.getBlocking())
    }

    @Test
    fun nullableSerializedListField_setBlockingAndGetBlocking() {
        val listPref = protoDatastore.nullableSerializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1))
        listPref.setBlocking(items)
        assertEquals(items, listPref.getBlocking())
    }

    @Test
    fun nullableSerializedListField_setBlockingNull() {
        val listPref = protoDatastore.nullableSerializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.setBlocking(listOf(TestItem("A", 1)))
        listPref.setBlocking(null)
        assertNull(listPref.getBlocking())
    }

    @Test
    fun nullableSerializedListField_resetToDefaultBlocking() {
        val listPref = protoDatastore.nullableSerializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.setBlocking(listOf(TestItem("A", 1)))
        listPref.resetToDefaultBlocking()
        assertNull(listPref.getBlocking())
    }

    @Test
    fun nullableSerializedListField_propertyDelegation() {
        val listPref = protoDatastore.nullableSerializedListField(
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
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
