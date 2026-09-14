package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.proto.core.TestProtoData
import io.github.arthurkun.generic.datastore.proto.core.TestProtoDataSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(InternalGenericDatastoreApi::class)
class InMemoryProtoDatastoreTest {

    @Test
    fun inMemory_fieldRoundTrip() = runTest {
        val datastore = createInMemoryProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
        )
        val name = datastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })

        assertEquals("", name.get())
        name.set("Alice")
        assertEquals("Alice", name.get())
    }

    @Test
    fun inMemory_batchWriteAppliesAllFieldsAtOnce() = runTest {
        val datastore = createInMemoryProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
        )
        val id = datastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        val name = datastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })

        datastore.batchWrite {
            set(id, 7)
            set(name, "batched")
        }

        assertEquals(7, id.get())
        assertEquals("batched", name.get())
    }

    @Test
    fun inMemory_stateIsPrivateToInstance() = runTest {
        val first = createInMemoryProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
        )
        val second = createInMemoryProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
        )

        first.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })
            .set("only-in-first")

        val firstName = first.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })
        val secondName = second.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })
        assertEquals("only-in-first", firstName.get())
        assertEquals("", secondName.get())
    }

    @Test
    fun inMemory_dataReflectsWrites() = runTest {
        val datastore = createInMemoryProtoDatastore(
            serializer = TestProtoDataSerializer,
            defaultValue = TestProtoData(),
        )
        val replacement = TestProtoData(id = 3, name = "whole")

        datastore.data().set(replacement)

        assertEquals(replacement, datastore.data().asFlow().first())
    }
}
