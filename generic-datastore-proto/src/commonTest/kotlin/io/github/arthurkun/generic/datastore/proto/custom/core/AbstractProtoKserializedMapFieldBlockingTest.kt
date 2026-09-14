package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.kserializedMapField
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedMapFieldBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>

    @Test
    fun kserializedMapField_blockingRoundTrip() {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.setBlocking(mapOf("a" to 1))
        assertEquals(mapOf("a" to 1), mapPref.getBlocking())

        mapPref.resetToDefaultBlocking()
        assertEquals(emptyMap(), mapPref.getBlocking())
    }

    @Test
    fun kserializedMapField_propertyDelegation() {
        var delegated: Map<String, Int> by protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        assertEquals(emptyMap(), delegated)

        delegated = mapOf("b" to 2)
        assertEquals(mapOf("b" to 2), delegated)
    }
}
