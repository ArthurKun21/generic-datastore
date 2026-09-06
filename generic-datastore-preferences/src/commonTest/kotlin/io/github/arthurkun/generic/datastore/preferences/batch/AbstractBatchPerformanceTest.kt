package io.github.arthurkun.generic.datastore.preferences.batch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.measureTime

abstract class AbstractBatchPerformanceTest {

    abstract val preferenceDatastore: GenericPreferencesDatastore
    abstract val dataStore: DataStore<Preferences>
    abstract val testDispatcher: TestDispatcher

    private data class TimingResult(
        val label: String,
        val duration: Duration,
        val operationCount: Int,
    ) {
        val perOperation: Duration get() = duration / operationCount
    }

    private fun printComparison(
        testName: String,
        normal: TimingResult,
        batch: TimingResult,
    ) {
        val speedup = normal.duration / batch.duration
        println()
        println("=== $testName ===")
        println("  Normal : ${normal.duration} total, ${normal.perOperation}/op (${normal.operationCount} ops)")
        println("  Batch  : ${batch.duration} total, ${batch.perOperation}/op (${batch.operationCount} ops)")
        val rounded = (speedup * 100).toLong() / 100.0
        println("  Speedup: ${rounded}x faster with batch")
        println()
    }

    private suspend fun compareWrites(count: Int) {
        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_write${count}_$it", 0)
        }
        val normalTime = measureTime {
            normalPrefs.forEachIndexed { i, pref -> pref.set(i * 10) }
        }
        normalPrefs.forEachIndexed { i, pref -> assertEquals(i * 10, pref.get()) }

        val handles = mutableListOf<BatchPref<Int>>()
        val batch = prefBatch {
            repeat(count) { i -> handles += int("perf_batch_write${count}_$i", 0) }
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite(batch) {
                handles.forEachIndexed { i, handle -> set(handle, i * 10) }
            }
        }
        handles.forEachIndexed { i, handle ->
            assertEquals(i * 10, preferenceDatastore.int(handle.key, 0).get())
        }

        printComparison(
            "Write $count preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    private suspend fun compareReads(count: Int) {
        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_read${count}_$it", 0)
        }
        normalPrefs.forEachIndexed { i, pref -> pref.set(i * 10) }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.get() }
        }

        val handles = mutableListOf<BatchPref<Int>>()
        val batch = prefBatch {
            repeat(count) { i -> handles += int("perf_batch_read${count}_$i", 0) }
        }
        handles.forEachIndexed { i, handle ->
            preferenceDatastore.int(handle.key, 0).set(i * 10)
        }
        val batchTime = measureTime {
            val values = preferenceDatastore.batchRead(batch)
            handles.forEach { handle -> values[handle] }
        }
        val batchValues = preferenceDatastore.batchRead(batch)
        handles.forEachIndexed { i, handle -> assertEquals(i * 10, batchValues[handle]) }

        printComparison(
            "Read $count preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    private suspend fun compareUpdates(count: Int) {
        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_update${count}_$it", 0)
        }
        normalPrefs.forEach { pref -> pref.set(0) }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.update { value -> value + 1 } }
        }
        normalPrefs.forEachIndexed { i, pref -> assertEquals(1, pref.get()) }

        val handles = mutableListOf<BatchPref<Int>>()
        val batch = prefBatch {
            repeat(count) { i -> handles += int("perf_batch_update${count}_$i", 0) }
        }
        handles.forEach { handle -> preferenceDatastore.int(handle.key, 0).set(0) }
        val batchTime = measureTime {
            preferenceDatastore.batchUpdate(batch) {
                handles.forEach { handle -> update(handle) { value -> value + 1 } }
            }
        }
        handles.forEach { handle ->
            assertEquals(1, preferenceDatastore.int(handle.key, 0).get())
        }

        printComparison(
            "Update $count preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    private suspend fun compareDeletes(count: Int) {
        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_delete${count}_$it", 0)
        }
        normalPrefs.forEachIndexed { i, pref -> pref.set(i) }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.delete() }
        }
        normalPrefs.forEachIndexed { i, pref -> assertEquals(0, pref.get()) }

        val handles = mutableListOf<BatchPref<Int>>()
        val batch = prefBatch {
            repeat(count) { i -> handles += int("perf_batch_delete${count}_$i", 0) }
        }
        handles.forEachIndexed { i, handle -> preferenceDatastore.int(handle.key, 0).set(i) }
        val batchTime = measureTime {
            preferenceDatastore.batchDelete(batch)
        }
        val values = preferenceDatastore.batchRead(batch)
        handles.forEach { handle -> assertEquals(0, values[handle]) }

        printComparison(
            "Delete $count preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    private suspend fun compareResetToDefault(count: Int) {
        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_reset${count}_$it", 7)
        }
        normalPrefs.forEachIndexed { i, pref -> pref.set(i + 100) }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.resetToDefault() }
        }
        normalPrefs.forEach { pref -> assertEquals(7, pref.get()) }

        val handles = mutableListOf<BatchPref<Int>>()
        val batch = prefBatch {
            repeat(count) { i -> handles += int("perf_batch_reset${count}_$i", 7) }
        }
        handles.forEachIndexed { i, handle ->
            preferenceDatastore.int(handle.key, 7).set(i + 100)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite(batch) {
                handles.forEach { handle -> resetToDefault(handle) }
            }
        }
        handles.forEach { handle ->
            assertEquals(7, preferenceDatastore.int(handle.key, 7).get())
        }

        printComparison(
            "Reset $count preferences to default",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    // ---- Write performance ----

    @Test
    fun performanceComparison_write5Preferences() = runTest(testDispatcher) { compareWrites(5) }

    @Test
    fun performanceComparison_write10Preferences() = runTest(testDispatcher) { compareWrites(10) }

    @Test
    fun performanceComparison_write25Preferences() = runTest(testDispatcher) { compareWrites(25) }

    @Test
    fun performanceComparison_write50Preferences() = runTest(testDispatcher) { compareWrites(50) }

    // ---- Read performance ----

    @Test
    fun performanceComparison_read5Preferences() = runTest(testDispatcher) { compareReads(5) }

    @Test
    fun performanceComparison_read10Preferences() = runTest(testDispatcher) { compareReads(10) }

    @Test
    fun performanceComparison_read25Preferences() = runTest(testDispatcher) { compareReads(25) }

    @Test
    fun performanceComparison_read50Preferences() = runTest(testDispatcher) { compareReads(50) }

    // ---- Update performance ----

    @Test
    fun performanceComparison_update5Preferences() = runTest(testDispatcher) { compareUpdates(5) }

    @Test
    fun performanceComparison_update10Preferences() = runTest(testDispatcher) { compareUpdates(10) }

    @Test
    fun performanceComparison_update25Preferences() = runTest(testDispatcher) { compareUpdates(25) }

    // ---- Mixed-type performance ----

    @Test
    fun performanceComparison_writeMixedTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("perf_normal_mixed_string", "")
        val intPref = preferenceDatastore.int("perf_normal_mixed_int", 0)
        val boolPref = preferenceDatastore.bool("perf_normal_mixed_bool", false)
        val longPref = preferenceDatastore.long("perf_normal_mixed_long", 0L)

        val normalTime = measureTime {
            stringPref.set("mixed")
            intPref.set(7)
            boolPref.set(true)
            longPref.set(70L)
        }
        assertEquals("mixed", stringPref.get())
        assertEquals(7, intPref.get())

        var stringHandle: BatchPref<String>? = null
        var intHandle: BatchPref<Int>? = null
        var boolHandle: BatchPref<Boolean>? = null
        var longHandle: BatchPref<Long>? = null
        val batch = prefBatch {
            stringHandle = add(stringPref)
            intHandle = add(intPref)
            boolHandle = add(boolPref)
            longHandle = add(longPref)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite(batch) {
                set(requireNotNull(stringHandle), "mixed")
                set(requireNotNull(intHandle), 7)
                set(requireNotNull(boolHandle), true)
                set(requireNotNull(longHandle), 70L)
            }
        }
        assertEquals("mixed", stringPref.get())
        assertEquals(7, intPref.get())
        assertEquals(true, boolPref.get())
        assertEquals(70L, longPref.get())

        printComparison(
            "Write mixed types",
            TimingResult("Normal", normalTime, 4),
            TimingResult("Batch", batchTime, 4),
        )
    }

    @Test
    fun performanceComparison_readMixedTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("perf_normal_mixed_read_string", "")
        val intPref = preferenceDatastore.int("perf_normal_mixed_read_int", 0)
        val boolPref = preferenceDatastore.bool("perf_normal_mixed_read_bool", false)
        val longPref = preferenceDatastore.long("perf_normal_mixed_read_long", 0L)
        stringPref.set("mixed")
        intPref.set(7)
        boolPref.set(true)
        longPref.set(70L)

        val normalTime = measureTime {
            stringPref.get()
            intPref.get()
            boolPref.get()
            longPref.get()
        }

        var stringHandle: BatchPref<String>? = null
        var intHandle: BatchPref<Int>? = null
        var boolHandle: BatchPref<Boolean>? = null
        var longHandle: BatchPref<Long>? = null
        val batch = prefBatch {
            stringHandle = add(stringPref)
            intHandle = add(intPref)
            boolHandle = add(boolPref)
            longHandle = add(longPref)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchRead(batch).toMap()
        }
        val values = preferenceDatastore.batchRead(batch)
        assertEquals("mixed", values[requireNotNull(stringHandle)])
        assertEquals(7, values[requireNotNull(intHandle)])
        assertEquals(true, values[requireNotNull(boolHandle)])
        assertEquals(70L, values[requireNotNull(longHandle)])

        printComparison(
            "Read mixed types",
            TimingResult("Normal", normalTime, 4),
            TimingResult("Batch", batchTime, 4),
        )
    }

    // ---- Delete / reset performance ----

    @Test
    fun performanceComparison_delete10Preferences() = runTest(testDispatcher) { compareDeletes(10) }

    @Test
    fun performanceComparison_resetToDefault10Preferences() = runTest(testDispatcher) {
        compareResetToDefault(10)
    }
}
