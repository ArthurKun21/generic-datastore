package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import io.github.arthurkun.generic.datastore.proto.nullableKserializedMapField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableKserializedMapFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableKserializedMapField_getReturnsNullWhenUnset() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        assertNull(mapPref.get())
    }

    @Test
    fun nullableKserializedMapField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        val entries = mapOf("a" to 1, "b" to 2)
        mapPref.set(entries)
        assertEquals(entries, mapPref.get())
    }

    @Test
    fun nullableKserializedMapField_setNullRestoresNull() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.set(null)
        assertNull(mapPref.get())
    }

    @Test
    fun nullableKserializedMapField_malformedJsonFallsBackToNull() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        protoDatastore.data().set(TestCustomFieldProtoData(nullableJsonMapRaw = "not-json"))
        assertNull(mapPref.get())
    }

    @Test
    fun nullableKserializedMapField_deleteResetsToNull() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.delete()
        assertNull(mapPref.get())
    }

    @Test
    fun nullableKserializedMapField_updateAddsEntry() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.update { (it ?: emptyMap()) + ("b" to 2) }
        assertEquals(mapOf("a" to 1, "b" to 2), mapPref.get())
    }

    @Test
    fun nullableKserializedMapField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>?> = protoDatastore.nullableKserializedMapField(
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        assertNull(mapPref.asFlow().first())
        mapPref.set(mapOf("c" to 3))
        assertEquals(mapOf("c" to 3), mapPref.asFlow().first())
    }
}
