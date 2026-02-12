package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableKserializedListFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableKserializedListField_getReturnsNull() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableKserializedListField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item_list",
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        assertNull(listPref.get())
    }

    @Test
    fun nullableKserializedListField_setNonNullList() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableKserializedListField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item_list",
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1))
        listPref.set(items)
        assertEquals(items, listPref.get())
    }

    @Test
    fun nullableKserializedListField_setNull() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableKserializedListField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item_list",
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.set(null)
        assertNull(listPref.get())
    }

    @Test
    fun nullableKserializedListField_deleteResetsToNull() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableKserializedListField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item_list",
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.delete()
        assertNull(listPref.get())
    }

    @Test
    fun nullableKserializedListField_asFlowNullTransitions() = runTest(testDispatcher) {
        val listPref = protoDatastore.nullableKserializedListField<TestCustomFieldProtoData, TestItem>(
            key = "nullable_item_list",
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
