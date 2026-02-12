package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedSetFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedSetField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val setPref = protoDatastore.kserializedSetField<TestCustomFieldProtoData, TestItem>(
            key = "item_set",
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        assertEquals(emptySet(), setPref.get())
    }

    @Test
    fun kserializedSetField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val setPref = protoDatastore.kserializedSetField<TestCustomFieldProtoData, TestItem>(
            key = "item_set",
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        val items = setOf(TestItem("A", 1), TestItem("B", 2))
        setPref.set(items)
        assertEquals(items, setPref.get())
    }

    @Test
    fun kserializedSetField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val setPref = protoDatastore.kserializedSetField<TestCustomFieldProtoData, TestItem>(
            key = "item_set",
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        assertEquals(emptySet(), setPref.asFlow().first())
        setPref.set(setOf(TestItem("C", 3)))
        assertEquals(setOf(TestItem("C", 3)), setPref.asFlow().first())
    }

    @Test
    fun kserializedSetField_updateAddsElement() = runTest(testDispatcher) {
        val setPref = protoDatastore.kserializedSetField<TestCustomFieldProtoData, TestItem>(
            key = "item_set",
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.set(setOf(TestItem("A", 1)))
        setPref.update { it + TestItem("B", 2) }
        assertEquals(setOf(TestItem("A", 1), TestItem("B", 2)), setPref.get())
    }

    @Test
    fun kserializedSetField_deleteResetsToEmpty() = runTest(testDispatcher) {
        val setPref = protoDatastore.kserializedSetField<TestCustomFieldProtoData, TestItem>(
            key = "item_set",
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.set(setOf(TestItem("A", 1)))
        setPref.delete()
        assertEquals(emptySet(), setPref.get())
    }

    @Test
    fun kserializedSetField_resetToDefault() = runTest(testDispatcher) {
        val setPref = protoDatastore.kserializedSetField<TestCustomFieldProtoData, TestItem>(
            key = "item_set",
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.set(setOf(TestItem("A", 1)))
        setPref.resetToDefault()
        assertEquals(emptySet(), setPref.get())
    }
}
