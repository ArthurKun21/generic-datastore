package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableField_blockingRoundTrip() {
        val fieldPref = protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        assertNull(fieldPref.getBlocking())

        fieldPref.setBlocking("value")
        assertEquals("value", fieldPref.getBlocking())

        fieldPref.resetToDefaultBlocking()
        assertNull(fieldPref.getBlocking())
    }

    @Test
    fun nullableField_propertyDelegation() {
        var delegated by protoDatastore.nullableField<String>(
            getter = { it.nullableJsonRaw },
            updater = { proto, value -> proto.copy(nullableJsonRaw = value) },
        )
        assertNull(delegated)

        delegated = "delegated"
        assertEquals("delegated", delegated)
    }
}
