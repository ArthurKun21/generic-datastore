package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.TestColor
import io.github.arthurkun.generic.datastore.proto.core.custom.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.nullableEnumField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableEnumFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableEnumField_getReturnsNullWhenNotSet() = runTest(testDispatcher) {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.get())
    }

    @Test
    fun nullableEnumField_setToNonNull() = runTest(testDispatcher) {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.set(TestColor.GREEN)
        assertEquals(TestColor.GREEN, colorPref.get())
    }

    @Test
    fun nullableEnumField_setToNull() = runTest(testDispatcher) {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.set(TestColor.GREEN)
        colorPref.set(null)
        assertNull(colorPref.get())
    }

    @Test
    fun nullableEnumField_deleteResetsToNull() = runTest(testDispatcher) {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.set(TestColor.BLUE)
        colorPref.delete()
        assertNull(colorPref.get())
    }

    @Test
    fun nullableEnumField_asFlowNullTransitions() = runTest(testDispatcher) {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        assertNull(colorPref.asFlow().first())
        colorPref.set(TestColor.RED)
        assertEquals(TestColor.RED, colorPref.asFlow().first())
        colorPref.set(null)
        assertNull(colorPref.asFlow().first())
    }

    @Test
    fun nullableEnumField_updateFromNullToValue() = runTest(testDispatcher) {
        val colorPref: ProtoPreference<TestColor?> = protoDatastore.nullableEnumField(
            key = "nullable_color",
            getter = { it.nullableEnumRaw },
            updater = { proto, raw -> proto.copy(nullableEnumRaw = raw) },
        )
        colorPref.update { TestColor.BLUE }
        assertEquals(TestColor.BLUE, colorPref.get())
    }
}
