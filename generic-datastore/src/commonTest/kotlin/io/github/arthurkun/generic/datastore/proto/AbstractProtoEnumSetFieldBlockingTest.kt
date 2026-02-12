package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoEnumSetFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun enumSetField_getBlockingReturnsEmptyDefault() {
        val colorsPref = protoDatastore.enumSetField<TestCustomFieldProtoData, TestColor>(
            key = "colors",
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        assertEquals(emptySet(), colorsPref.getBlocking())
    }

    @Test
    fun enumSetField_setBlockingAndGetBlocking() {
        val colorsPref = protoDatastore.enumSetField<TestCustomFieldProtoData, TestColor>(
            key = "colors",
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.setBlocking(setOf(TestColor.RED, TestColor.GREEN))
        assertEquals(setOf(TestColor.RED, TestColor.GREEN), colorsPref.getBlocking())
    }

    @Test
    fun enumSetField_resetToDefaultBlocking() {
        val colorsPref = protoDatastore.enumSetField<TestCustomFieldProtoData, TestColor>(
            key = "colors",
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.setBlocking(setOf(TestColor.BLUE))
        colorsPref.resetToDefaultBlocking()
        assertEquals(emptySet(), colorsPref.getBlocking())
    }

    @Test
    fun enumSetField_propertyDelegation() {
        val colorsPref = protoDatastore.enumSetField<TestCustomFieldProtoData, TestColor>(
            key = "colors",
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        var colors: Set<TestColor> by colorsPref
        colors = setOf(TestColor.RED, TestColor.BLUE)
        assertEquals(setOf(TestColor.RED, TestColor.BLUE), colors)
    }
}
