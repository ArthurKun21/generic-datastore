package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class AbstractProtoDatastoreTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun data_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        assertEquals(TestProtoData(), pref.get())
    }

    @Test
    fun data_setAndGetValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = TestProtoData(id = 1, name = "Alice")
        pref.set(value)
        assertEquals(value, pref.get())
    }

    @Test
    fun data_observeDefaultValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = pref.asFlow().first()
        assertEquals(TestProtoData(), value)
    }

    @Test
    fun data_observeSetValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        val value = TestProtoData(id = 2, name = "Bob")
        pref.set(value)
        val observed = pref.asFlow().first()
        assertEquals(value, observed)
    }

    @Test
    fun data_deleteResetsToDefault() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestProtoData(id = 3, name = "Charlie"))
        assertEquals(TestProtoData(id = 3, name = "Charlie"), pref.get())

        pref.delete()
        assertEquals(TestProtoData(), pref.get())
    }

    @Test
    fun data_updateValue() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestProtoData(id = 1, name = "Start"))
        pref.update { it.copy(id = it.id + 10, name = "Updated") }
        assertEquals(TestProtoData(id = 11, name = "Updated"), pref.get())
    }

    @Test
    fun data_resetToDefault() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        pref.set(TestProtoData(id = 5, name = "Changed"))
        assertEquals(TestProtoData(id = 5, name = "Changed"), pref.get())

        pref.resetToDefault()
        assertEquals(TestProtoData(), pref.get())
    }

    @Test
    fun data_keyReturnsConfiguredKey() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        assertEquals("proto_datastore", pref.key())
    }

    @Test
    fun data_defaultKey() = runTest(testDispatcher) {
        val pref = protoDatastore.data()
        assertEquals("proto_datastore", pref.key())
    }

    @Test
    fun data_customKey() = runTest(testDispatcher) {
        val customDatastore = GenericProtoDatastore(
            datastore = protoDatastore.datastore,
            defaultValue = TestProtoData(),
            key = "custom_key",
        )
        val pref = customDatastore.data()
        assertEquals("custom_key", pref.key())
    }

    @Test
    fun protoPreference_blankKeyThrows() = runTest(testDispatcher) {
        val blankKeyDatastore = GenericProtoDatastore(
            datastore = protoDatastore.datastore,
            defaultValue = TestProtoData(),
            key = " ",
        )
        assertFailsWith<IllegalArgumentException> {
            blankKeyDatastore.data()
        }
    }
}
