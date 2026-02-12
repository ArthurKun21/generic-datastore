package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoEnumFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun enumField_getBlockingReturnsDefault() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        assertEquals(TestColor.RED, colorPref.getBlocking())
    }

    @Test
    fun enumField_setBlockingAndGetBlocking() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.GREEN)
        assertEquals(TestColor.GREEN, colorPref.getBlocking())
    }

    @Test
    fun enumField_resetToDefaultBlocking() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.BLUE)
        colorPref.resetToDefaultBlocking()
        assertEquals(TestColor.RED, colorPref.getBlocking())
    }

    @Test
    fun enumField_propertyDelegation() {
        val colorPref = protoDatastore.enumField<TestCustomFieldProtoData, TestColor>(
            key = "color",
            defaultValue = TestColor.RED,
            getter = { it.enumRaw },
            updater = { proto, raw -> proto.copy(enumRaw = raw) },
        )
        var color: TestColor by colorPref
        color = TestColor.BLUE
        assertEquals(TestColor.BLUE, color)
    }
}
