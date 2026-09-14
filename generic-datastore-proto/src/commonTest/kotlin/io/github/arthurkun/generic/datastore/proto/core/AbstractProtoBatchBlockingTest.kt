package io.github.arthurkun.generic.datastore.proto.core

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoBatchBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>

    @Test
    fun batchWriteBlocking_setsMultipleFields() {
        val id = protoDatastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        val name = protoDatastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })

        protoDatastore.batchWriteBlocking {
            set(id, 3)
            set(name, "blocking")
        }

        assertEquals(3, id.getBlocking())
        assertEquals("blocking", name.getBlocking())
    }

    @Test
    fun batchUpdateBlocking_readsSeeEarlierWrites() {
        val id = protoDatastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })

        protoDatastore.batchUpdateBlocking {
            update(id) { it + 1 }
            update(id) { it + 1 }
        }

        assertEquals(2, id.getBlocking())
    }
}
