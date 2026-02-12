package io.github.arthurkun.generic.datastore.proto.core.customSet

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.core.custom.TestColor
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.enumSetField
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoEnumSetFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun enumSetField_getBlockingReturnsEmptyDefault() {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        assertEquals(emptySet(), colorsPref.getBlocking())
    }

    @Test
    fun enumSetField_setBlockingAndGetBlocking() {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.setBlocking(setOf(TestColor.RED, TestColor.GREEN))
        assertEquals(setOf(TestColor.RED, TestColor.GREEN), colorsPref.getBlocking())
    }

    @Test
    fun enumSetField_resetToDefaultBlocking() {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        colorsPref.setBlocking(setOf(TestColor.BLUE))
        colorsPref.resetToDefaultBlocking()
        assertEquals(emptySet(), colorsPref.getBlocking())
    }

    @Test
    fun enumSetField_propertyDelegation() {
        val colorsPref = protoDatastore.enumSetField(
            defaultValue = emptySet<TestColor>(),
            getter = { it.enumSetRaw },
            updater = { proto, raw -> proto.copy(enumSetRaw = raw) },
        )
        var colors: Set<TestColor> by colorsPref
        colors = setOf(TestColor.RED, TestColor.BLUE)
        assertEquals(setOf(TestColor.RED, TestColor.BLUE), colors)
    }
}
