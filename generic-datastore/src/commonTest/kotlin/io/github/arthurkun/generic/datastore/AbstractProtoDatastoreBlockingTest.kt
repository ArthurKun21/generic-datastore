package io.github.arthurkun.generic.datastore

import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractProtoDatastoreBlockingTest {

    abstract val protoDatastore: GenericProtoDatastore<TestProtoData>

    @Test
    fun data_getBlockingReturnsDefault() {
        val pref = protoDatastore.data()
        assertEquals(TestProtoData(), pref.getBlocking())
    }

    @Test
    fun data_setBlockingAndGetBlocking() {
        val pref = protoDatastore.data()
        val value = TestProtoData(id = 1, name = "BlockingUser")
        pref.setBlocking(value)
        assertEquals(value, pref.getBlocking())
    }

    @Test
    fun data_resetToDefaultBlocking() {
        val pref = protoDatastore.data()
        pref.setBlocking(TestProtoData(id = 99, name = "ToReset"))
        assertEquals(TestProtoData(id = 99, name = "ToReset"), pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(TestProtoData(), pref.getBlocking())
    }

    @Test
    fun data_delegation() {
        val pref = protoDatastore.data()
        var delegated: TestProtoData by pref

        val newValue = TestProtoData(id = 42, name = "Delegated")
        delegated = newValue
        assertEquals(newValue, delegated)
        assertEquals(newValue, pref.getBlocking())

        pref.resetToDefaultBlocking()
        assertEquals(TestProtoData(), delegated)
        assertEquals(TestProtoData(), pref.getBlocking())
    }
}
