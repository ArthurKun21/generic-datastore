package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.enumField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class AbstractProtoEnumFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun enumField_getReturnsDefaultWhenNotSet() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_setAndGet() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.BLUE)
        assertEquals(TestColor.BLUE, colorPref.get())
    }

    @Test
    fun enumField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.asFlow().first())
        colorPref.set(TestColor.GREEN)
        assertEquals(TestColor.GREEN, colorPref.asFlow().first())
    }

    @Test
    fun enumField_updateAtomically() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.RED)
        colorPref.update { if (it == TestColor.RED) TestColor.GREEN else it }
        assertEquals(TestColor.GREEN, colorPref.get())
    }

    @Test
    fun enumField_deleteResetsToDefault() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.BLUE)
        colorPref.delete()
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_resetToDefault() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.set(TestColor.GREEN)
        colorPref.resetToDefault()
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_keyReturnsConfiguredKey() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "my_color_key",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals("my_color_key", colorPref.key())
    }

    @Test
    fun enumField_blankKeyThrows() {
        assertFailsWith<IllegalArgumentException> {
            protoDatastore.enumField(
                key = " ",
                defaultValue = TestColor.RED,
                getter = { it.enumRaw },
                updater = { proto, raw -> proto.copy(enumRaw = raw) },
            )
        }
    }

    @Test
    fun enumField_invalidStoredStringFallsBackToDefault() = runTest(testDispatcher) {
        val rawPref = protoDatastore.field(
            key = "enum_raw_direct",
            defaultValue = "",
            getter = { it.enumRaw },
            updater = { proto, value -> proto.copy(enumRaw = value) },
        )
        rawPref.set("INVALID_VALUE")
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.get())
    }

    @Test
    fun enumField_doesNotAffectOtherFields() = runTest(testDispatcher) {
        val colorPref = protoDatastore.enumField(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        val jsonPref = protoDatastore.field(
            key = "json_raw",
            defaultValue = "",
            getter = { it.jsonRaw },
            updater = { proto, value -> proto.copy(jsonRaw = value) },
        )
        jsonPref.set("keepme")
        colorPref.set(TestColor.BLUE)
        assertEquals("keepme", jsonPref.get())
    }
}
