package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableSerializedMapFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableSerializedMapField_blockingRoundTrip() {
        val mapPref = protoDatastore.nullableSerializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        assertNull(mapPref.getBlocking())

        mapPref.setBlocking(mapOf("a" to 1))
        assertEquals(mapOf("a" to 1), mapPref.getBlocking())

        mapPref.resetToDefaultBlocking()
        assertNull(mapPref.getBlocking())
    }
}
