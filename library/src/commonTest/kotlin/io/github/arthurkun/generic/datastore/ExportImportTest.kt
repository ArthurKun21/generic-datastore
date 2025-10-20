package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.Preference.Companion.appStateKey
import io.github.arthurkun.generic.datastore.Preference.Companion.privateKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for export and import functionality.
 */
class ExportImportTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) {
            createTempFile("test_export_import", ".preferences_pb").also { it.deleteOnExit() }
        }
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun export_includesNormalPreferences() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("normal_key", "")
        stringPref.set("test_value")

        val exported = preferenceDatastore.export()

        assertTrue(exported.containsKey("normal_key"))
        assertEquals(JsonPrimitive("test_value"), exported["normal_key"])
    }

    @Test
    fun export_excludesPrivatePreferencesByDefault() = runTest(testDispatcher) {
        val privatePref = preferenceDatastore.string(privateKey("secret"), "")
        privatePref.set("secret_value")

        val exported = preferenceDatastore.export(exportPrivate = false)

        assertFalse(exported.containsKey(privateKey("secret")))
    }

    @Test
    fun export_includesPrivatePreferencesWhenRequested() = runTest(testDispatcher) {
        val privateKey = privateKey("secret")
        val privatePref = preferenceDatastore.string(privateKey, "")
        privatePref.set("secret_value")

        val exported = preferenceDatastore.export(exportPrivate = true)

        assertTrue(exported.containsKey(privateKey))
        assertEquals(JsonPrimitive("secret_value"), exported[privateKey])
    }

    @Test
    fun export_excludesAppStatePreferencesByDefault() = runTest(testDispatcher) {
        val appStateKey = appStateKey("ui_state")
        val appStatePref = preferenceDatastore.string(appStateKey, "")
        appStatePref.set("state_value")

        val exported = preferenceDatastore.export(exportAppState = false)

        assertFalse(exported.containsKey(appStateKey))
    }

    @Test
    fun export_includesAppStatePreferencesWhenRequested() = runTest(testDispatcher) {
        val appStateKey = appStateKey("ui_state")
        val appStatePref = preferenceDatastore.string(appStateKey, "")
        appStatePref.set("state_value")

        val exported = preferenceDatastore.export(exportAppState = true)

        assertTrue(exported.containsKey(appStateKey))
        assertEquals(JsonPrimitive("state_value"), exported[appStateKey])
    }

    @Test
    fun export_handlesDifferentDataTypes() = runTest(testDispatcher) {
        val stringPref = preferenceDatastore.string("string_key", "")
        val intPref = preferenceDatastore.int("int_key", 0)
        val longPref = preferenceDatastore.long("long_key", 0L)
        val floatPref = preferenceDatastore.float("float_key", 0f)
        val boolPref = preferenceDatastore.bool("bool_key", false)

        stringPref.set("test")
        intPref.set(42)
        longPref.set(999L)
        floatPref.set(3.14f)
        boolPref.set(true)

        val exported = preferenceDatastore.export()

        assertEquals(JsonPrimitive("test"), exported["string_key"])
        assertEquals(JsonPrimitive(42), exported["int_key"])
        assertEquals(JsonPrimitive(999L), exported["long_key"])
        assertEquals(JsonPrimitive(3.14f), exported["float_key"])
        assertEquals(JsonPrimitive(true), exported["bool_key"])
    }

    @Test
    fun import_restoresStringPreferences() = runTest(testDispatcher) {
        val data = mapOf("imported_string" to "imported_value")

        preferenceDatastore.import(data)

        val stringPref = preferenceDatastore.string("imported_string", "")
        assertEquals("imported_value", stringPref.get())
    }

    @Test
    fun import_restoresIntPreferences() = runTest(testDispatcher) {
        val data = mapOf("imported_int" to 123)

        preferenceDatastore.import(data)

        val intPref = preferenceDatastore.int("imported_int", 0)
        assertEquals(123, intPref.get())
    }

    @Test
    fun import_restoresLongPreferences() = runTest(testDispatcher) {
        val data = mapOf("imported_long" to 9999L)

        preferenceDatastore.import(data)

        val longPref = preferenceDatastore.long("imported_long", 0L)
        assertEquals(9999L, longPref.get())
    }

    @Test
    fun import_restoresFloatPreferences() = runTest(testDispatcher) {
        val data = mapOf("imported_float" to 2.71f)

        preferenceDatastore.import(data)

        val floatPref = preferenceDatastore.float("imported_float", 0f)
        assertEquals(2.71f, floatPref.get())
    }

    @Test
    fun import_restoresBooleanPreferences() = runTest(testDispatcher) {
        val data = mapOf("imported_bool" to true)

        preferenceDatastore.import(data)

        val boolPref = preferenceDatastore.bool("imported_bool", false)
        assertEquals(true, boolPref.get())
    }

    @Test
    fun import_restoresStringSetPreferences() = runTest(testDispatcher) {
        val data = mapOf("imported_set" to listOf("a", "b", "c"))

        preferenceDatastore.import(data)

        val setPref = preferenceDatastore.stringSet("imported_set", emptySet())
        assertEquals(setOf("a", "b", "c"), setPref.get())
    }

    @Test
    fun import_handlesInvalidData() = runTest(testDispatcher) {
        // Import should not throw even with problematic data
        val data = mapOf(
            "valid_key" to "valid_value",
            "problem_key" to Any(), // Unsupported type
        )

        preferenceDatastore.import(data)

        val validPref = preferenceDatastore.string("valid_key", "")
        assertEquals("valid_value", validPref.get())
    }

    @Test
    fun exportAndImport_roundTrip() = runTest(testDispatcher) {
        // Set up some preferences
        val stringPref = preferenceDatastore.string("string_key", "")
        val intPref = preferenceDatastore.int("int_key", 0)
        val boolPref = preferenceDatastore.bool("bool_key", false)

        stringPref.set("test_value")
        intPref.set(42)
        boolPref.set(true)

        // Export
        val exported = preferenceDatastore.export()

        // Create a new datastore
        val newDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) {
            createTempFile("test_import", ".preferences_pb").also { it.deleteOnExit() }
        }
        val newPreferenceDatastore = GenericPreferenceDatastore(newDataStore)

        // Convert exported data to Map<String, Any>
        val importData = exported.mapValues { (_, value) ->
            when (value) {
                is JsonPrimitive -> {
                    value.content.toIntOrNull() ?: value.content.toBooleanStrictOrNull() ?: value.content
                }
                else -> value.toString()
            }
        }

        // Import to new datastore
        newPreferenceDatastore.import(importData)

        // Verify
        val newStringPref = newPreferenceDatastore.string("string_key", "")
        val newIntPref = newPreferenceDatastore.int("int_key", 0)
        val newBoolPref = newPreferenceDatastore.bool("bool_key", false)

        assertEquals("test_value", newStringPref.get())
        assertEquals(42, newIntPref.get())
        assertEquals(true, newBoolPref.get())
    }
}
