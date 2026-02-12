package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.kserializedListField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedListFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedListField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        assertEquals(emptyList(), listPref.get())
    }

    @Test
    fun kserializedListField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        val items = listOf(TestItem("A", 1), TestItem("B", 2))
        listPref.set(items)
        assertEquals(items, listPref.get())
    }

    @Test
    fun kserializedListField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        assertEquals(emptyList(), listPref.asFlow().first())
        listPref.set(listOf(TestItem("C", 3)))
        assertEquals(listOf(TestItem("C", 3)), listPref.asFlow().first())
    }

    @Test
    fun kserializedListField_updateAppendsElement() = runTest(testDispatcher) {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.update { it + TestItem("B", 2) }
        assertEquals(listOf(TestItem("A", 1), TestItem("B", 2)), listPref.get())
    }

    @Test
    fun kserializedListField_deleteResetsToEmptyDefault() = runTest(testDispatcher) {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.delete()
        assertEquals(emptyList(), listPref.get())
    }

    @Test
    fun kserializedListField_resetToDefault() = runTest(testDispatcher) {
        val listPref = protoDatastore.kserializedListField(
            defaultValue = emptyList<TestItem>(),
            getter = { it.jsonListRaw },
            updater = { proto, raw -> proto.copy(jsonListRaw = raw) },
        )
        listPref.set(listOf(TestItem("A", 1)))
        listPref.resetToDefault()
        assertEquals(emptyList(), listPref.get())
    }
}
