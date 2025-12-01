package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val TEST_DATASTORE_NAME = "test_android_enhanced_datastore"

@Serializable
private data class AndroidTestData(val id: Int, val name: String, val active: Boolean = true)

@RunWith(AndroidJUnit4::class)
class AndroidEnhancedFeaturesTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var datastoreManager: DatastoreManager
    private lateinit var testContext: Context
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE_NAME) },
        )
        datastoreManager = DatastoreManager(dataStore, CacheConfig(enabled = true, maxSize = 50))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        val dataStoreFile =
            File(testContext.filesDir, "datastore/${TEST_DATASTORE_NAME}.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        testScope.cancel()
    }

    // Test getAndSet functionality
    @Test
    fun testGetAndSet_returnsOldValue() = runTest(testDispatcher) {
        val pref = datastoreManager.string("testGetAndSet", "initial")
        pref.set("firstValue")

        val oldValue = pref.getAndSet("secondValue")

        assertEquals("firstValue", oldValue)
        assertEquals("secondValue", pref.get())
    }

    @Test
    fun testGetAndSet_withDefaultValue() = runTest(testDispatcher) {
        val pref = datastoreManager.int("testGetAndSetInt", 42)

        val oldValue = pref.getAndSet(100)

        assertEquals(42, oldValue)
        assertEquals(100, pref.get())
    }

    // Test batch operations
    @Test
    fun testBatchSet_setsMultiplePreferences() = runTest(testDispatcher) {
        val pref1 = datastoreManager.string("batch1", "")
        val pref2 = datastoreManager.int("batch2", 0)
        val pref3 = datastoreManager.bool("batch3", false)

        val operations: Map<Prefs<*>, Any?> = mapOf(
            pref1 to "value1",
            pref2 to 42,
            pref3 to true,
        )

        datastoreManager.batchSet(operations)

        assertEquals("value1", pref1.get())
        assertEquals(42, pref2.get())
        assertEquals(true, pref3.get())
    }

    @Test
    fun testBatchGet_getsMultiplePreferences() = runTest(testDispatcher) {
        val pref1 = datastoreManager.string("batchGet1", "default1")
        val pref2 = datastoreManager.int("batchGet2", 10)
        val pref3 = datastoreManager.bool("batchGet3", false)

        pref1.set("testValue")
        pref2.set(99)
        pref3.set(true)

        val results = datastoreManager.batchGet(listOf(pref1, pref2, pref3))

        assertEquals("testValue", results[pref1])
        assertEquals(99, results[pref2])
        assertEquals(true, results[pref3])
    }

    @Test
    fun testBatchDelete_deletesMultiplePreferences() = runTest(testDispatcher) {
        val pref1 = datastoreManager.string("batchDelete1", "default1")
        val pref2 = datastoreManager.int("batchDelete2", 10)

        pref1.set("value1")
        pref2.set(50)

        datastoreManager.batchDelete(listOf(pref1, pref2))

        assertEquals("default1", pref1.get())
        assertEquals(10, pref2.get())
    }

    // Test custom serialized preferences
    @Test
    fun testCustomSerializedPreference_setAndGet() = runTest(testDispatcher) {
        val defaultData = AndroidTestData(0, "default")
        val pref = datastoreManager.serialized<AndroidTestData>(
            key = "testCustomSerialized",
            defaultValue = defaultData,
            serializer = { "${it.id}|${it.name}|${it.active}" },
            deserializer = { str ->
                val parts = str.split("|")
                AndroidTestData(parts[0].toInt(), parts[1], parts[2].toBoolean())
            },
        )

        val testData = AndroidTestData(1, "test", true)
        pref.set(testData)

        val retrieved = pref.get()
        assertEquals(testData, retrieved)
    }

    @Test
    fun testCustomSerializedPreference_flow() = runTest(testDispatcher) {
        val defaultData = AndroidTestData(0, "default")
        val pref = datastoreManager.serialized<AndroidTestData>(
            key = "testCustomSerializedFlow",
            defaultValue = defaultData,
            serializer = { "${it.id}|${it.name}|${it.active}" },
            deserializer = { str ->
                val parts = str.split("|")
                AndroidTestData(parts[0].toInt(), parts[1], parts[2].toBoolean())
            },
        )

        val testData = AndroidTestData(2, "flow test", false)
        pref.set(testData)

        val flowValue = pref.asFlow().first()
        assertEquals(testData, flowValue)
    }

    // Test thread safety
    @Test
    fun testThreadSafety_concurrentWrites() = runTest(testDispatcher) {
        val pref = datastoreManager.int("concurrentTest", 0)

        val jobs = (1..20).map { value ->
            async {
                pref.set(value)
            }
        }
        jobs.forEach { it.await() }

        val finalValue = pref.get()
        assert(finalValue in 1..20)
    }

    // Test import/export
    @Test
    fun testExportAndImport() = runTest(testDispatcher) {
        val pref1 = datastoreManager.string("exportTest1", "")
        val pref2 = datastoreManager.int("exportTest2", 0)

        pref1.set("exported")
        pref2.set(123)

        val exported = datastoreManager.export(exportPrivate = true, exportAppState = true)
        assertEquals(2, exported.size)

        dataStore.edit { it.clear() }

        val importData = mapOf(
            "exportTest1" to "exported",
            "exportTest2" to 123,
        )
        datastoreManager.import(importData)

        assertEquals("exported", pref1.get())
        assertEquals(123, pref2.get())
    }

    // Test cache
    @Test
    fun testClearCache() = runTest(testDispatcher) {
        val pref = datastoreManager.string("clearCacheTest", "default")
        pref.set("value")

        assertEquals("value", pref.get())

        datastoreManager.clearCache()

        assertEquals("value", pref.get())
    }

    // Test MappedPreference with getAndSet
    @Test
    fun testMappedPreference_getAndSet() = runTest(testDispatcher) {
        val intPref = datastoreManager.int("mappedGetAndSet", 0)
        val mappedPref = intPref.map(
            defaultValue = "Mapped_0",
            convert = { "Mapped_$it" },
            reverse = { it.removePrefix("Mapped_").toInt() },
        )

        mappedPref.set("Mapped_50")
        val oldValue = mappedPref.getAndSet("Mapped_100")

        assertEquals("Mapped_50", oldValue)
        assertEquals("Mapped_100", mappedPref.get())
        assertEquals(100, intPref.get())
    }
}
