package io.github.arthurkun.generic.datastore.preferences.core

import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

abstract class AbstractBytesBlockingTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore

    @Test
    fun bytesPreference_resetToDefaultBlocking() {
        val bytesPref = preferenceDatastore.bytes("testBytesBlocking", byteArrayOf(1))
        bytesPref.setBlocking(byteArrayOf(2))
        assertContentEquals(byteArrayOf(2), bytesPref.getBlocking())

        bytesPref.resetToDefaultBlocking()
        assertContentEquals(byteArrayOf(1), bytesPref.getBlocking())
    }

    @Test
    fun nullableBytesPreference_blockingNullSemantics() {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytesBlocking")
        assertNull(bytesPref.getBlocking())

        bytesPref.setBlocking(byteArrayOf(1))
        assertContentEquals(byteArrayOf(1), bytesPref.getBlocking())

        bytesPref.resetToDefaultBlocking()
        assertNull(bytesPref.getBlocking())
    }

    @Test
    fun bytesPreference_propertyDelegation() {
        var delegated by preferenceDatastore.bytes("testBytesDelegated", byteArrayOf(1))
        assertContentEquals(byteArrayOf(1), delegated)

        delegated = byteArrayOf(2, 3)
        assertContentEquals(byteArrayOf(2, 3), delegated)
    }
}
