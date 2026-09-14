package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.proto.custom.core.TestCustomFieldProtoData
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(InternalGenericDatastoreApi::class)
abstract class AbstractDecodeFailureCallbackTest {

    abstract val protoDatastore: GenericProtoDatastore<TestCustomFieldProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun onDecodeFailure_reportsKeyAndError() = runTest(testDispatcher) {
        val datastoreKey = protoDatastore.data().key()
        val failures = mutableListOf<Pair<String, Throwable>>()
        val datastore = GenericProtoDatastore(
            datastore = protoDatastore.datastore,
            defaultValue = TestCustomFieldProtoData(),
            key = datastoreKey,
            onDecodeFailure = { key, error -> failures.add(key to error) },
        )
        val field = datastore.kserializedField(
            defaultValue = TestItemForDecode(0),
            serializer = TestItemForDecode.serializer(),
            getter = { it.jsonRaw },
            updater = { proto, raw -> proto.copy(jsonRaw = raw) },
        )

        field.set(TestItemForDecode(7))
        assertEquals(TestItemForDecode(7), field.get())
        assertTrue(failures.isEmpty())

        protoDatastore.data().set(TestCustomFieldProtoData(jsonRaw = "not-json"))

        assertEquals(TestItemForDecode(0), field.get())
        assertEquals(1, failures.size)
        assertEquals(datastoreKey, failures.single().first)
        assertTrue(failures.single().second is Exception)
    }
}

@kotlinx.serialization.Serializable
internal data class TestItemForDecode(val id: Int)
