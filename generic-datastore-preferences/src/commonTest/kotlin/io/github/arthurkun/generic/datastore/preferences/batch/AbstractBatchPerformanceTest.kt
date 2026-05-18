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

    // ---- Write performance ----

    @Test
    fun performanceComparison_write5Preferences() = runTest(testDispatcher) {
        val count = 5

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_write5_$it", 0)
        }
        val normalTime = measureTime {
            normalPrefs.forEachIndexed { i, pref -> pref.set(i * 10) }
        }
        normalPrefs.forEachIndexed { i, pref -> assertEquals(i * 10, pref.get()) }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_write5_$it", 0)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                batchPrefs.forEachIndexed { i, pref -> set(pref, i * 10) }
            }
        }
        batchPrefs.forEachIndexed { i, pref -> assertEquals(i * 10, pref.get()) }

        printComparison(
            "Write 5 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_write10Preferences() = runTest(testDispatcher) {
        val count = 10

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_write10_$it", 0)
        }
        val normalTime = measureTime {
            normalPrefs.forEachIndexed { i, pref -> pref.set(i * 10) }
        }
        normalPrefs.forEachIndexed { i, pref -> assertEquals(i * 10, pref.get()) }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_write10_$it", 0)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                batchPrefs.forEachIndexed { i, pref -> set(pref, i * 10) }
            }
        }
        batchPrefs.forEachIndexed { i, pref -> assertEquals(i * 10, pref.get()) }

        printComparison(
            "Write 10 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_write25Preferences() = runTest(testDispatcher) {
        val count = 25

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_write25_$it", 0)
        }
        val normalTime = measureTime {
            normalPrefs.forEachIndexed { i, pref -> pref.set(i) }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_write25_$it", 0)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                batchPrefs.forEachIndexed { i, pref -> set(pref, i) }
            }
        }

        printComparison(
            "Write 25 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_write50Preferences() = runTest(testDispatcher) {
        val count = 50

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_write50_$it", 0)
        }
        val normalTime = measureTime {
            normalPrefs.forEachIndexed { i, pref -> pref.set(i) }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_write50_$it", 0)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                batchPrefs.forEachIndexed { i, pref -> set(pref, i) }
            }
        }

        printComparison(
            "Write 50 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    // ---- Read performance ----

    @Test
    fun performanceComparison_read5Preferences() = runTest(testDispatcher) {
        val count = 5
        val prefs = (0 until count).map {
            preferenceDatastore.int("perf_read5_$it", it * 10)
        }

        val normalTime = measureTime {
            prefs.forEach { it.get() }
        }

        val batchTime = measureTime {
            preferenceDatastore.batchGet {
                prefs.map { get(it) }
            }
        }

        printComparison(
            "Read 5 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_read10Preferences() = runTest(testDispatcher) {
        val count = 10
        val prefs = (0 until count).map {
            preferenceDatastore.int("perf_read10_$it", it * 10)
        }

        val normalTime = measureTime {
            prefs.forEach { it.get() }
        }

        val batchTime = measureTime {
            preferenceDatastore.batchGet {
                prefs.map { get(it) }
            }
        }

        printComparison(
            "Read 10 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_read25Preferences() = runTest(testDispatcher) {
        val count = 25
        val prefs = (0 until count).map {
            preferenceDatastore.int("perf_read25_$it", it)
        }

        val normalTime = measureTime {
            prefs.forEach { it.get() }
        }

        val batchTime = measureTime {
            preferenceDatastore.batchGet {
                prefs.map { get(it) }
            }
        }

        printComparison(
            "Read 25 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_read50Preferences() = runTest(testDispatcher) {
        val count = 50
        val prefs = (0 until count).map {
            preferenceDatastore.int("perf_read50_$it", it)
        }

        val normalTime = measureTime {
            prefs.forEach { it.get() }
        }

        val batchTime = measureTime {
            preferenceDatastore.batchGet {
                prefs.map { get(it) }
            }
        }

        printComparison(
            "Read 50 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    // ---- Update (read-modify-write) performance ----

    @Test
    fun performanceComparison_update5Preferences() = runTest(testDispatcher) {
        val count = 5

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_update5_$it", it)
        }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.update { it + 1 } }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_update5_$it", it)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchUpdate {
                batchPrefs.forEach { pref -> update(pref) { it + 1 } }
            }
        }

        printComparison(
            "Update 5 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_update10Preferences() = runTest(testDispatcher) {
        val count = 10

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_update10_$it", it)
        }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.update { it + 1 } }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_update10_$it", it)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchUpdate {
                batchPrefs.forEach { pref -> update(pref) { it + 1 } }
            }
        }

        printComparison(
            "Update 10 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    @Test
    fun performanceComparison_update25Preferences() = runTest(testDispatcher) {
        val count = 25

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_update25_$it", it)
        }
        val normalTime = measureTime {
            normalPrefs.forEach { pref -> pref.update { it + 1 } }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_update25_$it", it)
        }
        val batchTime = measureTime {
            preferenceDatastore.batchUpdate {
                batchPrefs.forEach { pref -> update(pref) { it + 1 } }
            }
        }

        printComparison(
            "Update 25 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    // ---- Mixed-type performance ----

    @Test
    fun performanceComparison_writeMixedTypes() = runTest(testDispatcher) {
        val normalString = preferenceDatastore.string("perf_normal_mix_str", "")
        val normalInt = preferenceDatastore.int("perf_normal_mix_int", 0)
        val normalBool = preferenceDatastore.bool("perf_normal_mix_bool", false)
        val normalLong = preferenceDatastore.long("perf_normal_mix_long", 0L)
        val normalFloat = preferenceDatastore.float("perf_normal_mix_float", 0f)
        val normalDouble = preferenceDatastore.double("perf_normal_mix_double", 0.0)
        val normalStringSet = preferenceDatastore.stringSet("perf_normal_mix_sset", emptySet())

        val normalTime = measureTime {
            normalString.set("hello")
            normalInt.set(42)
            normalBool.set(true)
            normalLong.set(123456L)
            normalFloat.set(3.14f)
            normalDouble.set(2.718)
            normalStringSet.set(setOf("a", "b", "c"))
        }

        val batchString = preferenceDatastore.string("perf_batch_mix_str", "")
        val batchInt = preferenceDatastore.int("perf_batch_mix_int", 0)
        val batchBool = preferenceDatastore.bool("perf_batch_mix_bool", false)
        val batchLong = preferenceDatastore.long("perf_batch_mix_long", 0L)
        val batchFloat = preferenceDatastore.float("perf_batch_mix_float", 0f)
        val batchDouble = preferenceDatastore.double("perf_batch_mix_double", 0.0)
        val batchStringSet = preferenceDatastore.stringSet("perf_batch_mix_sset", emptySet())

        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                set(batchString, "hello")
                set(batchInt, 42)
                set(batchBool, true)
                set(batchLong, 123456L)
                set(batchFloat, 3.14f)
                set(batchDouble, 2.718)
                set(batchStringSet, setOf("a", "b", "c"))
            }
        }

        printComparison(
            "Write 7 mixed-type preferences",
            TimingResult("Normal", normalTime, 7),
            TimingResult("Batch", batchTime, 7),
        )
    }

    @Test
    fun performanceComparison_readMixedTypes() = runTest(testDispatcher) {
        val str = preferenceDatastore.string("perf_read_mix_str", "hello")
        val int = preferenceDatastore.int("perf_read_mix_int", 42)
        val bool = preferenceDatastore.bool("perf_read_mix_bool", true)
        val long = preferenceDatastore.long("perf_read_mix_long", 123456L)
        val float = preferenceDatastore.float("perf_read_mix_float", 3.14f)
        val double = preferenceDatastore.double("perf_read_mix_double", 2.718)
        val stringSet = preferenceDatastore.stringSet("perf_read_mix_sset", setOf("a", "b"))

        val normalTime = measureTime {
            str.get()
            int.get()
            bool.get()
            long.get()
            float.get()
            double.get()
            stringSet.get()
        }

        val batchTime = measureTime {
            preferenceDatastore.batchGet {
                get(str)
                get(int)
                get(bool)
                get(long)
                get(float)
                get(double)
                get(stringSet)
            }
        }

        printComparison(
            "Read 7 mixed-type preferences",
            TimingResult("Normal", normalTime, 7),
            TimingResult("Batch", batchTime, 7),
        )
    }

    // ---- Delete performance ----

    @Test
    fun performanceComparison_delete10Preferences() = runTest(testDispatcher) {
        val count = 10

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_del10_$it", it).also { p ->
                p.set(it * 100)
            }
        }
        val normalTime = measureTime {
            normalPrefs.forEach { it.delete() }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_del10_$it", it).also { p ->
                p.set(it * 100)
            }
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                batchPrefs.forEach { delete(it) }
            }
        }

        printComparison(
            "Delete 10 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }

    // ---- ResetToDefault performance ----

    @Test
    fun performanceComparison_resetToDefault10Preferences() = runTest(testDispatcher) {
        val count = 10

        val normalPrefs = (0 until count).map {
            preferenceDatastore.int("perf_normal_reset10_$it", it).also { p ->
                p.set(it * 100)
            }
        }
        val normalTime = measureTime {
            normalPrefs.forEach { it.resetToDefault() }
        }

        val batchPrefs = (0 until count).map {
            preferenceDatastore.int("perf_batch_reset10_$it", it).also { p ->
                p.set(it * 100)
            }
        }
        val batchTime = measureTime {
            preferenceDatastore.batchWrite {
                batchPrefs.forEach { resetToDefault(it) }
            }
        }

        printComparison(
            "ResetToDefault 10 preferences",
            TimingResult("Normal", normalTime, count),
            TimingResult("Batch", batchTime, count),
        )
    }
}
