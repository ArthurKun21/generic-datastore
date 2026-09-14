package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.batch.BatchPref
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

abstract class AbstractBytesTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    @Test
    fun bytesPreference_defaultValueWhenNotSet() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytes", byteArrayOf(1, 2, 3))
        assertContentEquals(byteArrayOf(1, 2, 3), bytesPref.get())
    }

    @Test
    fun bytesPreference_setAndGetValue() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytes", ByteArray(0))
        bytesPref.set(byteArrayOf(4, 5, 6))
        assertContentEquals(byteArrayOf(4, 5, 6), bytesPref.get())
    }

    @Test
    fun bytesPreference_emptyArrayRoundTrip() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytesEmpty", byteArrayOf(9))
        bytesPref.set(ByteArray(0))
        assertContentEquals(ByteArray(0), bytesPref.get())
    }

    @Test
    fun bytesPreference_observeValue() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytesFlow", ByteArray(0))
        bytesPref.set(byteArrayOf(7))
        assertContentEquals(byteArrayOf(7), bytesPref.asFlow().first())
    }

    @Test
    fun bytesPreference_deleteValue() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytesDelete", byteArrayOf(1))
        bytesPref.set(byteArrayOf(2))
        assertContentEquals(byteArrayOf(2), bytesPref.get())
        bytesPref.delete()
        assertContentEquals(byteArrayOf(1), bytesPref.get())
    }

    @Test
    fun bytesPreference_resetToDefault() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytesReset", byteArrayOf(1))
        bytesPref.set(byteArrayOf(2))
        bytesPref.resetToDefault()
        assertContentEquals(byteArrayOf(1), bytesPref.get())
    }

    @Test
    fun bytesPreference_usesNativeByteArrayKey() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytesKey", ByteArray(0))
        bytesPref.set(byteArrayOf(10, 20))

        val raw = dataStore.data.first()[byteArrayPreferencesKey("testBytesKey")]
        assertContentEquals(byteArrayOf(10, 20), raw)
    }

    @Test
    fun nullableBytesPreference_nullWhenNotSet() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytes")
        assertNull(bytesPref.get())
    }

    @Test
    fun nullableBytesPreference_setAndGetValue() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytes")
        bytesPref.set(byteArrayOf(1, 2))
        assertContentEquals(byteArrayOf(1, 2), bytesPref.get())
    }

    @Test
    fun nullableBytesPreference_setNullRemovesKey() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytes")
        bytesPref.set(byteArrayOf(1))
        bytesPref.set(null)
        assertNull(bytesPref.get())
    }

    @Test
    fun nullableBytesPreference_deleteValue() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytes")
        bytesPref.set(byteArrayOf(3))
        bytesPref.delete()
        assertNull(bytesPref.get())
    }

    @Test
    fun nullableBytesPreference_resetToDefault() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytes")
        bytesPref.set(byteArrayOf(3))
        bytesPref.resetToDefault()
        assertNull(bytesPref.get())
    }

    @Test
    fun nullableBytesPreference_updateValue() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.nullableBytes("testNullableBytes")
        bytesPref.update { current -> (current ?: ByteArray(0)) + byteArrayOf(99) }
        assertContentEquals(byteArrayOf(99), bytesPref.get())
    }

    @Test
    fun bytesPreference_backupRoundTrip() = runTest(testDispatcher) {
        val bytesPref = preferenceDatastore.bytes("testBytesBackup", ByteArray(0))
        val nullableBytesPref = preferenceDatastore.nullableBytes("testNullableBytesBackup")
        bytesPref.set(byteArrayOf(1, 2, 3, 4))
        nullableBytesPref.set(byteArrayOf(-1, 127))

        val backup = preferenceDatastore.exportAsData()

        preferenceDatastore.clearAll()
        preferenceDatastore.importData(backup)
        assertContentEquals(byteArrayOf(1, 2, 3, 4), bytesPref.get())
        assertContentEquals(byteArrayOf(-1, 127), nullableBytesPref.get())
    }

    @Test
    fun bytesPreference_batchRoundTrip() = runTest(testDispatcher) {
        var bytesHandle: BatchPref<ByteArray>? = null
        var nullableBytesHandle: BatchPref<ByteArray?>? = null

        preferenceDatastore.batchWrite {
            bytesHandle = bytes("testBatchBytes", ByteArray(0))
            nullableBytesHandle = nullableBytes("testBatchNullableBytes")
            set(requireNotNull(bytesHandle), byteArrayOf(5, 6))
            set(requireNotNull(nullableBytesHandle), byteArrayOf(8))
        }

        val values = preferenceDatastore.batchReadValues {
            bytesHandle = bytes("testBatchBytes", ByteArray(0))
            nullableBytesHandle = nullableBytes("testBatchNullableBytes")
        }
        assertContentEquals(byteArrayOf(5, 6), values[requireNotNull(bytesHandle)])
        assertContentEquals(byteArrayOf(8), values[requireNotNull(nullableBytesHandle)])
    }
}
