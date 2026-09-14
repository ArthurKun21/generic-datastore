package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.nullableKserializedMapField
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableKserializedMapFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun nullableKserializedMapField_blockingRoundTrip() {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
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
