package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.custom.core.TestItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableSerializedFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    private val itemSerializer: (TestItem) -> String = { Json.encodeToString(it) }
    private val itemDeserializer: (String) -> TestItem = { Json.decodeFromString(it) }

    @Test
    fun nullableSerializedField_getReturnsNullWhenNotSet() = runTest(testDispatcher) {
        val itemPref = protoDatastore.nullableSerializedField(
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        assertNull(itemPref.get())
    }

    @Test
    fun nullableSerializedField_setToNonNull() = runTest(testDispatcher) {
        val itemPref = protoDatastore.nullableSerializedField(
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.set(item)
        assertEquals(item, itemPref.get())
    }

    @Test
    fun nullableSerializedField_setToNull() = runTest(testDispatcher) {
        val itemPref = protoDatastore.nullableSerializedField(
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "Widget", quantity = 5))
        itemPref.set(null)
        assertNull(itemPref.get())
    }

    @Test
    fun nullableSerializedField_deleteResetsToNull() = runTest(testDispatcher) {
        val itemPref = protoDatastore.nullableSerializedField(
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "Widget", quantity = 5))
        itemPref.delete()
        assertNull(itemPref.get())
    }

    @Test
    fun nullableSerializedField_asFlowNullTransitions() = runTest(testDispatcher) {
        val itemPref = protoDatastore.nullableSerializedField(
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        assertNull(itemPref.asFlow().first())
        itemPref.set(TestItem(name = "Gadget", quantity = 3))
        assertEquals(TestItem(name = "Gadget", quantity = 3), itemPref.asFlow().first())
        itemPref.set(null)
        assertNull(itemPref.asFlow().first())
    }

    @Test
    fun nullableSerializedField_updateFromNullToValue() = runTest(testDispatcher) {
        val itemPref = protoDatastore.nullableSerializedField(
            serializer = itemSerializer,
            deserializer = itemDeserializer,
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.update { TestItem(name = "Created", quantity = 1) }
        assertEquals(TestItem(name = "Created", quantity = 1), itemPref.get())
    }
}
