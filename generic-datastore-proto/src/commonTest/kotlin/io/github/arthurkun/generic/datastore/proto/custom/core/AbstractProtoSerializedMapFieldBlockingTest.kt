package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedMapFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun serializedMapField_blockingRoundTrip() {
        val mapPref = protoDatastore.serializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.setBlocking(mapOf("a" to 1))
        assertEquals(mapOf("a" to 1), mapPref.getBlocking())

        mapPref.resetToDefaultBlocking()
        assertEquals(emptyMap(), mapPref.getBlocking())
    }
}
