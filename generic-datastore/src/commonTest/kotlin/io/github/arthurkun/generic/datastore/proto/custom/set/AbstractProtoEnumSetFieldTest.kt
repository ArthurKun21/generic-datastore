package io.github.arthurkun.generic.datastore.proto.custom.set

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.custom.core.TestColor
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.enumSetField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoEnumSetFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun enumSetField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        assertEquals(emptySet(), colorsPref.get())
    }

    @Test
    fun enumSetField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.set(setOf(TestColor.RED, TestColor.BLUE))
        assertEquals(setOf(TestColor.RED, TestColor.BLUE), colorsPref.get())
    }

    @Test
    fun enumSetField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        assertEquals(emptySet(), colorsPref.asFlow().first())
        colorsPref.set(setOf(TestColor.GREEN))
        assertEquals(setOf(TestColor.GREEN), colorsPref.asFlow().first())
    }

    @Test
    fun enumSetField_updateAddsElement() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.set(setOf(TestColor.RED))
        colorsPref.update { it + TestColor.BLUE }
        assertEquals(setOf(TestColor.RED, TestColor.BLUE), colorsPref.get())
    }

    @Test
    fun enumSetField_updateRemovesElement() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.set(setOf(TestColor.RED, TestColor.BLUE))
        colorsPref.update { it - TestColor.RED }
        assertEquals(setOf(TestColor.BLUE), colorsPref.get())
    }

    @Test
    fun enumSetField_deleteResetsToEmpty() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.set(setOf(TestColor.RED, TestColor.GREEN))
        colorsPref.delete()
        assertEquals(emptySet(), colorsPref.get())
    }

    @Test
    fun enumSetField_resetToDefault() = runTest(testDispatcher) {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.set(setOf(TestColor.BLUE))
        colorsPref.resetToDefault()
        assertEquals(emptySet(), colorsPref.get())
    }
}
