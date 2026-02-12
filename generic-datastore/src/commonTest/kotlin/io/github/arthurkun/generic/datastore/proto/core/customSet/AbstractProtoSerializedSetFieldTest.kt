package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.core.custom.TestItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedSetFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    private val elemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val elemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun serializedSetField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        assertEquals(emptySet(), setPref.get())
    }

    @Test
    fun serializedSetField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        val items = setOf(TestItem("A", 1), TestItem("B", 2))
        setPref.set(items)
        assertEquals(items, setPref.get())
    }

    @Test
    fun serializedSetField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        assertEquals(emptySet(), setPref.asFlow().first())
        setPref.set(setOf(TestItem("C", 3)))
        assertEquals(setOf(TestItem("C", 3)), setPref.asFlow().first())
    }

    @Test
    fun serializedSetField_updateAddsElement() = runTest(testDispatcher) {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.set(setOf(TestItem("A", 1)))
        setPref.update { it + TestItem("B", 2) }
        assertEquals(setOf(TestItem("A", 1), TestItem("B", 2)), setPref.get())
    }

    @Test
    fun serializedSetField_deleteResetsToEmpty() = runTest(testDispatcher) {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.set(setOf(TestItem("A", 1)))
        setPref.delete()
        assertEquals(emptySet(), setPref.get())
    }

    @Test
    fun serializedSetField_resetToDefault() = runTest(testDispatcher) {
        val setPref = protoDatastore.serializedSetField(
            key = "ser_item_set",
            serializer = elemSerializer,
            deserializer = elemDeserializer,
            getter = { it.jsonSetRaw },
            updater = { proto, raw -> proto.copy(jsonSetRaw = raw) },
        )
        setPref.set(setOf(TestItem("A", 1)))
        setPref.resetToDefault()
        assertEquals(emptySet(), setPref.get())
    }
}
