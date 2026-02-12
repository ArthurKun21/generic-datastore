package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.core.custom.TestItem
import io.github.arthurkun.generic.datastore.proto.nullableKserializedListField
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
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        assertNull(listPref.get())
    }

    @Test
    fun nullableKserializedListField_setNonNullList() = runTest(testDispatcher) {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1))
        listPref.set(items)
        assertEquals(items, listPref.get())
    }

    @Test
    fun nullableKserializedListField_setNull() = runTest(testDispatcher) {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.set(null)
        assertNull(listPref.get())
    }

    @Test
    fun nullableKserializedListField_deleteResetsToNull() = runTest(testDispatcher) {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
            getter = { it.nullableJsonListRaw },
            updater = { proto, raw -> proto.copy(nullableJsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.delete()
        assertNull(listPref.get())
    }

    @Test
    fun nullableKserializedListField_asFlowNullTransitions() = runTest(testDispatcher) {
        val listPref: ProtoPreference<List<TestItem>?> = protoDatastore.nullableKserializedListField(
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
