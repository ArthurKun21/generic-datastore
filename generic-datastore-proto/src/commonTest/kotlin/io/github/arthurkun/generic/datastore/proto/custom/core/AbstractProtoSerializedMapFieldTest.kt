package io.github.arthurkun.generic.datastore.proto.custom.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoSerializedMapFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun serializedMapField_getReturnsEmptyDefault() = runTest(testDispatcher) {
        val mapPref = protoDatastore.serializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        assertEquals(emptyMap(), mapPref.get())
    }

    @Test
    fun serializedMapField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val mapPref = protoDatastore.serializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        val entries = mapOf("a" to 1, "b" to 2)
        mapPref.set(entries)
        assertEquals(entries, mapPref.get())
    }

    @Test
    fun serializedMapField_skipsUndecodableEntries() = runTest(testDispatcher) {
        val mapPref = protoDatastore.serializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        protoDatastore.data().set(TestCustomFieldProtoData(jsonMapRaw = """{"a":"1","b":"bad"}"""))
        assertEquals(mapOf("a" to 1), mapPref.get())
    }

    @Test
    fun serializedMapField_deleteResetsToEmptyDefault() = runTest(testDispatcher) {
        val mapPref = protoDatastore.serializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.delete()
        assertEquals(emptyMap(), mapPref.get())
    }

    @Test
    fun serializedMapField_asFlowEmitsUpdates() = runTest(testDispatcher) {
        val mapPref = protoDatastore.serializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.jsonMapRaw },
            updater = { proto, raw -> proto.copy(jsonMapRaw = raw) },
        )
        mapPref.set(mapOf("z" to 26))
        assertEquals(mapOf("z" to 26), mapPref.asFlow().first())
    }
}
