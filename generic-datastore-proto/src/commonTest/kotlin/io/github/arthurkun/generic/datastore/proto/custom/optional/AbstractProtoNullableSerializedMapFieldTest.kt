package io.github.arthurkun.generic.datastore.proto.custom.optional

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractProtoNullableSerializedMapFieldTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun nullableSerializedMapField_getReturnsNullWhenUnset() = runTest(testDispatcher) {
        val mapPref = protoDatastore.nullableSerializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        assertNull(mapPref.get())
    }

    @Test
    fun nullableSerializedMapField_setAndGetRoundTrip() = runTest(testDispatcher) {
        val mapPref = protoDatastore.nullableSerializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        val entries = mapOf("a" to 1)
        mapPref.set(entries)
        assertEquals(entries, mapPref.get())
    }

    @Test
    fun nullableSerializedMapField_skipsUndecodableEntries() = runTest(testDispatcher) {
        val mapPref = protoDatastore.nullableSerializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        protoDatastore.data().set(TestCustomFieldProtoData(nullableJsonMapRaw = """{"a":"1","b":"bad"}"""))
        assertEquals(mapOf("a" to 1), mapPref.get())
    }

    @Test
    fun nullableSerializedMapField_setNullRestoresNull() = runTest(testDispatcher) {
        val mapPref = protoDatastore.nullableSerializedMapField<String, Int>(
            keySerializer = { it },
            keyDeserializer = { it },
            valueSerializer = { it.toString() },
            valueDeserializer = { it.toInt() },
            getter = { it.nullableJsonMapRaw },
            updater = { proto, raw -> proto.copy(nullableJsonMapRaw = raw) },
        )
        mapPref.set(mapOf("a" to 1))
        mapPref.set(null)
        assertNull(mapPref.get())
    }
}
