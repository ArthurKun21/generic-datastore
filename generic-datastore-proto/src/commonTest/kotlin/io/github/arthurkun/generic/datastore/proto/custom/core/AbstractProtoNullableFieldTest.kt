package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableField_getReturnsNullWhenUnset() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        assertNull(fieldPref.get())
    }

    @Test
    fun nullableField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        fieldPref.set("stored")
        assertEquals("stored", fieldPref.get())
    }

    @Test
    fun nullableField_setNullRestoresNull() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        fieldPref.set("stored")
        fieldPref.set(null)
        assertNull(fieldPref.get())
    }

    @Test
    fun nullableField_updateTransformsValue() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        fieldPref.set("a")
        fieldPref.update { current -> current + "b" }
        assertEquals("ab", fieldPref.get())
    }

    @Test
    fun nullableField_updateToNullRemovesValue() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        fieldPref.set("a")
        fieldPref.update { null }
        assertNull(fieldPref.get())
    }

    @Test
    fun nullableField_deleteResetsToNull() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        fieldPref.set("a")
        fieldPref.delete()
        assertNull(fieldPref.get())
    }

    @Test
    fun nullableField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        assertNull(fieldPref.asFlow().first())
        fieldPref.set("flow")
        assertEquals("flow", fieldPref.asFlow().first())
    }
}
