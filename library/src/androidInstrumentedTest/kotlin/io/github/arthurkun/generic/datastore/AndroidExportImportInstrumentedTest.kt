package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arthurkun.generic.datastore.Preference.Companion.appStateKey
import io.github.arthurkun.generic.datastore.Preference.Companion.privateKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Android instrumented tests for export/import functionality.
 */
@RunWith(AndroidJUnit4::class)
class AndroidExportImportInstrumentedTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
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
            produceFile = { testContext.preferencesDataStoreFile("test_export_import_android") },
        )
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        val dataStoreFile =
            File(testContext.filesDir, "datastore/test_export_import_android.preferences_pb")
        if (dataStoreFile.exists()) {
            dataStoreFile.delete()
        }
        testScope.cancel()
    }

    @Test
    fun export_includesAllPreferenceTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("string_key", "")
        val intPref = preferenceDatastore.int("int_key", 0)
        val longPref = preferenceDatastore.long("long_key", 0L)
        val floatPref = preferenceDatastore.float("float_key", 0f)
        val boolPref = preferenceDatastore.bool("bool_key", false)

        stringPref.set("test_string")
        intPref.set(123)
        longPref.set(456L)
        floatPref.set(7.89f)
        boolPref.set(true)

        val exported = preferenceDatastore.export()

        assertEquals(5, exported.size)
        assertEquals(JsonPrimitive("test_string"), exported["string_key"])
        assertEquals(JsonPrimitive(123), exported["int_key"])
        assertEquals(JsonPrimitive(456L), exported["long_key"])
        assertEquals(JsonPrimitive(7.89f), exported["float_key"])
        assertEquals(JsonPrimitive(true), exported["bool_key"])
    }

    @Test
    fun export_respectsPrivacySettings() = runTest(testDispatcher) {
        val normalKey = "normal_preference"
        val secretKey = privateKey("password")
        val stateKey = appStateKey("ui_scroll_position")

        val normalPref = preferenceDatastore.string(normalKey, "")
        val secretPref = preferenceDatastore.string(secretKey, "")
        val statePref = preferenceDatastore.int(stateKey, 0)

        normalPref.set("public_data")
        secretPref.set("secret_data")
        statePref.set(42)

        // Export without private and app state
        val exported = preferenceDatastore.export(exportPrivate = false, exportAppState = false)

        assertTrue(exported.containsKey(normalKey))
        assertFalse(exported.containsKey(secretKey))
        assertFalse(exported.containsKey(stateKey))
    }

    @Test
    fun export_includesPrivateWhenRequested() = runTest(testDispatcher) {
        val secretKey = privateKey("token")
        val secretPref = preferenceDatastore.string(secretKey, "")
        secretPref.set("secret_token_value")

        val exported = preferenceDatastore.export(exportPrivate = true)

        assertTrue(exported.containsKey(secretKey))
        assertEquals(JsonPrimitive("secret_token_value"), exported[secretKey])
    }

    @Test
    fun export_includesAppStateWhenRequested() = runTest(testDispatcher) {
        val stateKey = appStateKey("current_tab")
        val statePref = preferenceDatastore.int(stateKey, 0)
        statePref.set(3)

        val exported = preferenceDatastore.export(exportAppState = true)

        assertTrue(exported.containsKey(stateKey))
        assertEquals(JsonPrimitive(3), exported[stateKey])
    }

    @Test
    fun import_restoresAllDataTypes() = runTest(testDispatcher) {
        val data = mapOf(
            "string" to "imported",
            "int" to 999,
            "long" to 8888L,
            "float" to 1.23f,
            "bool" to true,
        )

        preferenceDatastore.import(data)

        val stringPref = preferenceDatastore.string("string", "")
        val intPref = preferenceDatastore.int("int", 0)
        val longPref = preferenceDatastore.long("long", 0L)
        val floatPref = preferenceDatastore.float("float", 0f)
        val boolPref = preferenceDatastore.bool("bool", false)

        assertEquals("imported", stringPref.get())
        assertEquals(999, intPref.get())
        assertEquals(8888L, longPref.get())
        assertEquals(1.23f, floatPref.get())
        assertEquals(true, boolPref.get())
    }

    @Test
    fun import_handlesStringSet() = runTest(testDispatcher) {
        val data = mapOf("tags" to listOf("tag1", "tag2", "tag3"))

        preferenceDatastore.import(data)

        val setPref = preferenceDatastore.stringSet("tags", emptySet())
        assertEquals(setOf("tag1", "tag2", "tag3"), setPref.get())
    }

    @Test
    fun import_handlesInvalidDataGracefully() = runTest(testDispatcher) {
        val data = mapOf(
            "valid" to "value",
            "invalid" to Any(), // This should be handled gracefully
        )

        // Should not throw
        preferenceDatastore.import(data)

        val validPref = preferenceDatastore.string("valid", "")
        assertEquals("value", validPref.get())
    }

    @Test
    fun roundTrip_preservesAllData() = runTest(testDispatcher) {
        // Set up original data
        val stringPref = preferenceDatastore.string("name", "")
        val intPref = preferenceDatastore.int("age", 0)
        val boolPref = preferenceDatastore.bool("active", false)

        stringPref.set("John Doe")
        intPref.set(30)
        boolPref.set(true)

        // Export
        val exported = preferenceDatastore.export()

        // Create new datastore
        val newDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile("test_new_datastore") },
        )
        val newPreferenceDatastore = GenericPreferenceDatastore(newDataStore)

        // Convert and import
        val importData = exported.mapValues { (_, value) ->
            when (value) {
                is JsonPrimitive -> {
                    value.content.toIntOrNull()
                        ?: value.content.toBooleanStrictOrNull()
                        ?: value.content
                }
                else -> value.toString()
            }
        }
        newPreferenceDatastore.import(importData)

        // Verify
        val newStringPref = newPreferenceDatastore.string("name", "")
        val newIntPref = newPreferenceDatastore.int("age", 0)
        val newBoolPref = newPreferenceDatastore.bool("active", false)

        assertEquals("John Doe", newStringPref.get())
        assertEquals(30, newIntPref.get())
        assertEquals(true, newBoolPref.get())

        // Cleanup
        File(testContext.filesDir, "datastore/test_new_datastore.preferences_pb").delete()
    }
}
