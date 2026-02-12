package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.TestColor
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.nullableEnumField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableEnumFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableEnumField_getBlockingReturnsNull() {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_setBlockingAndGetBlocking() {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.setBlocking(TestColor.BLUE)
        assertEquals(TestColor.BLUE, colorPref.getBlocking())
    }

    @Test
    fun nullableEnumField_setBlockingNull() {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
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
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
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
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
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
