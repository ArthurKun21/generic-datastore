package io.github.arthurkun.generic.datastore.proto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableEnumFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableEnumField_getBlockingReturnsNull() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_setBlockingAndGetBlocking() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.BLUE)
        assertEquals(TestColor.BLUE, colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_setBlockingNull() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.RED)
        colorPref.setBlocking(null)
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_resetToDefaultBlocking() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.GREEN)
        colorPref.resetToDefaultBlocking()
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_propertyDelegation() {
        val colorPref = protoDatastore.nullableEnumField<TestCustomFieldProtoData, TestColor>(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        var color: TestColor? by colorPref
        assertNull(color)
        color = TestColor.BLUE
        assertEquals(TestColor.BLUE, color)
        color = null
        assertNull(color)
    }
}
