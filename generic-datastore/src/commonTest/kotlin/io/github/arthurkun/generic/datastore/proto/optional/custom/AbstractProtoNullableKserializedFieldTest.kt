package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.core.custom.TestItem
import io.github.arthurkun.generic.datastore.proto.nullableKserializedField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableKserializedFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableKserializedField_getReturnsNullWhenNotSet() = runTest(testDispatcher) {
        val itemPref: ProtoPreference<TestItem?> = protoDatastore.nullableKserializedField(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        assertNull(itemPref.get())
    }

    @Test
    fun nullableKserializedField_setToNonNull() = runTest(testDispatcher) {
        val itemPref: ProtoPreference<TestItem?> = protoDatastore.nullableKserializedField(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.set(item)
        assertEquals(item, itemPref.get())
    }

    @Test
    fun nullableKserializedField_setToNull() = runTest(testDispatcher) {
        val itemPref: ProtoPreference<TestItem?> = protoDatastore.nullableKserializedField(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "Widget", quantity = 5))
        itemPref.set(null)
        assertNull(itemPref.get())
    }

    @Test
    fun nullableKserializedField_deleteResetsToNull() = runTest(testDispatcher) {
        val itemPref: ProtoPreference<TestItem?> = protoDatastore.nullableKserializedField(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "Widget", quantity = 5))
        itemPref.delete()
        assertNull(itemPref.get())
    }

    @Test
    fun nullableKserializedField_asFlowNullTransitions() = runTest(testDispatcher) {
        val itemPref: ProtoPreference<TestItem?> = protoDatastore.nullableKserializedField(
            key = "nullable_item",
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
    fun nullableKserializedField_updateFromNullToValue() = runTest(testDispatcher) {
        val itemPref: ProtoPreference<TestItem?> = protoDatastore.nullableKserializedField(
            key = "nullable_item",
            getter = { it.nullableJsonRaw },
            updater = { proto, raw -> proto.copy(nullableJsonRaw = raw) },
        )
        itemPref.update { TestItem(name = "Created", quantity = 1) }
        assertEquals(TestItem(name = "Created", quantity = 1), itemPref.get())
    }
}
