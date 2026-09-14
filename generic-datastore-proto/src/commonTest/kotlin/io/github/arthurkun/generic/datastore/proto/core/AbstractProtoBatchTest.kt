package io.github.arthurkun.generic.datastore.proto.core

import io.github.arthurkun.generic.datastore.core.InternalGenericDatastoreApi
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(InternalGenericDatastoreApi::class)
abstract class AbstractProtoBatchTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun batchWrite_setsMultipleFieldsInOneTransaction() = runTest(testDispatcher) {
        val id = protoDatastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        val name = protoDatastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })

        protoDatastore.batchWrite {
            set(id, 42)
            set(name, "Alice")
        }

        assertEquals(42, id.get())
        assertEquals("Alice", name.get())
    }

    @Test
    fun batchWrite_resetToDefaultRestoresFieldDefault() = runTest(testDispatcher) {
        val name = protoDatastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })
        name.set("temporary")

        protoDatastore.batchWrite {
            resetToDefault(name)
        }

        assertEquals("", name.get())
    }

    @Test
    fun batchUpdate_readsSeeEarlierWrites() = runTest(testDispatcher) {
        val id = protoDatastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        val name = protoDatastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })

        protoDatastore.batchUpdate {
            set(id, 7)
            set(name, "user-${get(id)}")
        }

        assertEquals(7, id.get())
        assertEquals("user-7", name.get())
    }

    @Test
    fun batchUpdate_updateTransformsCurrentField() = runTest(testDispatcher) {
        val id = protoDatastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        id.set(10)

        protoDatastore.batchUpdate {
            update(id) { it + 5 }
        }

        assertEquals(15, id.get())
    }

    @Test
    fun batchWrite_acceptsWrapperSharingSameUnderlyingDatastore() = runTest(testDispatcher) {
        val sharedWrapper = GenericProtoDatastore(
            datastore = protoDatastore.datastore,
            defaultValue = TestProtoData(),
        )
        val name = sharedWrapper.field(
            defaultValue = "",
            getter = { it.name },
            updater = { p, v -> p.copy(name = v) },
        )

        protoDatastore.batchWrite {
            set(name, "shared")
        }

        assertEquals("shared", name.get())
    }

    @Test
    fun batchWrite_dataPreferenceReplacesWholeProto() = runTest(testDispatcher) {
        val replacement = TestProtoData(id = 9, name = "whole")

        protoDatastore.batchWrite {
            set(protoDatastore.data(), replacement)
        }

        assertEquals(replacement, protoDatastore.data().get())
    }

    @Test
    fun batchWrite_emitsSingleFlowUpdate() = runTest(testDispatcher) {
        val id = protoDatastore.field(defaultValue = 0, getter = { it.id }, updater = { p, v -> p.copy(id = v) })
        val name = protoDatastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })

        val protoFlow = protoDatastore.data().asFlow()
        protoFlow.first()

        protoDatastore.batchWrite {
            set(id, 1)
            set(name, "one-shot")
        }

        assertEquals(TestProtoData(id = 1, name = "one-shot"), protoDatastore.data().get())
        assertEquals(1, id.get())
        assertEquals("one-shot", name.get())
    }

    @Test
    fun batchWrite_rollsBackOnFailure() = runTest(testDispatcher) {
        val name = protoDatastore.field(defaultValue = "", getter = { it.name }, updater = { p, v -> p.copy(name = v) })
        name.set("before")

        assertFailsWith<IllegalStateException> {
            protoDatastore.batchWrite {
                set(name, "changed")
                error("boom")
            }
        }

        assertEquals("before", name.get())
    }
}
