package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.kserializedMapField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoKserializedMapFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun kserializedMapField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        assertEquals(emptyMap(), mapPref.get())
    }

    @Test
    fun kserializedMapField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        val entries = mapOf("a" to 1, "b" to 2)
        mapPref.set(entries)
        assertEquals(entries, mapPref.get())
    }

    @Test
    fun kserializedMapField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        assertEquals(emptyMap(), mapPref.asFlow().first())
        mapPref.set(mapOf("c" to 3))
        assertEquals(mapOf("c" to 3), mapPref.asFlow().first())
    }

    @Test
    fun kserializedMapField_updateAddsEntry() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.update { it + ("b" to 2) }
        assertEquals(mapOf("a" to 1, "b" to 2), mapPref.get())
    }

    @Test
    fun kserializedMapField_deleteResetsToEmptyDefault() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.delete()
        assertEquals(emptyMap(), mapPref.get())
    }

    @Test
    fun kserializedMapField_resetToDefault() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            defaultValue = mapOf("default" to -1),
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.resetToDefault()
        assertEquals(mapOf("default" to -1), mapPref.get())
    }

    @Test
    fun kserializedMapField_malformedJsonFallsBackToDefault() = runTest(testDispatcher) {
        val mapPref: ProtoPreference<Map<String, Int>> = protoDatastore.kserializedMapField(
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        protoDatastore.data().set(TestCustomFieldProtoData(jsonMapRaw = "not-json"))
        assertEquals(emptyMap(), mapPref.get())
    }
}
