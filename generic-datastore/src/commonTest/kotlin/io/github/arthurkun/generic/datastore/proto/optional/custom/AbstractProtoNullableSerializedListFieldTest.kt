package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.core.custom.TestItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableSerializedListFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    private val elemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val elemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun nullableSerializedListField_getReturnsNull() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableSerializedListField(
            key = "nullable_ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        assertNull(listPref.get())
    }

    @Test
    fun nullableSerializedListField_setNonNullList() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableSerializedListField(
            key = "nullable_ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1))
        listPref.set(items)
        assertEquals(items, listPref.get())
    }

    @Test
    fun nullableSerializedListField_setNull() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableSerializedListField(
            key = "nullable_ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.set(null)
        assertNull(listPref.get())
    }

    @Test
    fun nullableSerializedListField_deleteResetsToNull() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableSerializedListField(
            key = "nullable_ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.delete()
        assertNull(listPref.get())
    }

    @Test
    fun nullableSerializedListField_asFlowNullTransitions() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableSerializedListField(
            key = "nullable_ser_item_list",
            elementSerializer = elemSerializer,
            elementDeserializer = elemDeserializer,
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        assertNull(listPref.asFlow().first())
        listPref.set(listOf(TestItem("B", 2)))
        assertEquals(listOf(TestItem("B", 2)), listPref.asFlow().first())
        listPref.set(null)
        assertNull(listPref.asFlow().first())
    }
}
