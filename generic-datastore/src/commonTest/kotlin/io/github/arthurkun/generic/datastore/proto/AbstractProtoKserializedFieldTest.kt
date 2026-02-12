package io.github.arthurkun.generic.datastore.proto

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedField_getReturnsDefaultWhenNotSet() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val item = TestItem(name = "Widget", quantity = 5)
        itemPref.set(item)
        assertEquals(item, itemPref.get())
    }

    @Test
    fun kserializedField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.asFlow().first())
        itemPref.set(TestItem(name = "Gadget", quantity = 3))
        assertEquals(TestItem(name = "Gadget", quantity = 3), itemPref.asFlow().first())
    }

    @Test
    fun kserializedField_updateAtomically() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "Widget", quantity = 5))
        itemPref.update { it.copy(quantity = it.quantity + 1) }
        assertEquals(TestItem(name = "Widget", quantity = 6), itemPref.get())
    }

    @Test
    fun kserializedField_deleteResetsToDefault() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "ToDelete", quantity = 1))
        itemPref.delete()
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_resetToDefault() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        itemPref.set(TestItem(name = "ToReset", quantity = 1))
        itemPref.resetToDefault()
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_corruptedDataFallsBackToDefault() = runTest(testDispatcher) {
        val rawPref = protoDatastore.field(
            key = "json_raw_direct",
            defaultValue = "",
            getter = { it.jsonRaw },
            updater = { proto, value -> proto.copy(jsonRaw = value) },
        )
        rawPref.set("{not valid json!!")
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        assertEquals(TestItem(), itemPref.get())
    }

    @Test
    fun kserializedField_doesNotAffectOtherFields() = runTest(testDispatcher) {
        val itemPref = protoDatastore.kserializedField<TestCustomFieldProtoData, TestItem>(
            key = "item",
            defaultValue = TestItem(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )
        val enumRawPref = protoDatastore.field(
            key = "enum_raw",
            defaultValue = "",
            getter = { it.enumRaw },
            updater = { proto, value -> proto.copy(enumRaw = value) },
        )
        enumRawPref.set("KEEP")
        itemPref.set(TestItem(name = "Widget", quantity = 1))
        assertEquals("KEEP", enumRawPref.get())
    }
}
