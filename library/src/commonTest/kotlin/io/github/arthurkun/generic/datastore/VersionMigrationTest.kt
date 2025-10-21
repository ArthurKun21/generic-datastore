package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for version migration functionality.
 */
class VersionMigrationTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var migrationManager: PreferenceMigrationManager
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) {
            createTempFile("test_migration", ".preferences_pb").also { it.deleteOnExit() }
        }
        migrationManager = PreferenceMigrationManager(dataStore)
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun getCurrentVersion_returnsZeroByDefault() = runTest(testDispatcher) {
        val version = migrationManager.getCurrentVersion()
        assertEquals(0, version)
    }

    @Test
    fun needsMigration_returnsTrueWhenVersionBehind() = runTest(testDispatcher) {
        migrationManager.resetVersion(1)
        assertTrue(migrationManager.needsMigration(2))
    }

    @Test
    fun needsMigration_returnsFalseWhenVersionCurrent() = runTest(testDispatcher) {
        migrationManager.resetVersion(2)
        assertFalse(migrationManager.needsMigration(2))
    }

    @Test
    fun needsMigration_returnsFalseWhenVersionAhead() = runTest(testDispatcher) {
        migrationManager.resetVersion(3)
        assertFalse(migrationManager.needsMigration(2))
    }

    @Test
    fun migrate_executesSimpleMigration() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)
        val testPref = datastore.string("test_key", "")

        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { prefs ->
                    prefs.string("test_key", "").set("migrated_value")
                },
            ),
        )

        val success = migrationManager.migrate(1)

        assertTrue(success)
        assertEquals(1, migrationManager.getCurrentVersion())
        assertEquals("migrated_value", testPref.get())
    }

    @Test
    fun migrate_executesMultipleMigrations() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        migrationManager.addMigrations(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { prefs ->
                    prefs.string("v1_key", "").set("version_1")
                },
            ),
            PreferenceMigration(
                fromVersion = 1,
                toVersion = 2,
                migrate = { prefs ->
                    prefs.string("v2_key", "").set("version_2")
                },
            ),
            PreferenceMigration(
                fromVersion = 2,
                toVersion = 3,
                migrate = { prefs ->
                    prefs.string("v3_key", "").set("version_3")
                },
            ),
        )

        val success = migrationManager.migrate(3)

        assertTrue(success)
        assertEquals(3, migrationManager.getCurrentVersion())
        assertEquals("version_1", datastore.string("v1_key", "").get())
        assertEquals("version_2", datastore.string("v2_key", "").get())
        assertEquals("version_3", datastore.string("v3_key", "").get())
    }

    @Test
    fun migrate_skipsUnnecessaryMigrations() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)
        migrationManager.resetVersion(1)

        migrationManager.addMigrations(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { prefs ->
                    prefs.string("should_not_run", "").set("skipped")
                },
            ),
            PreferenceMigration(
                fromVersion = 1,
                toVersion = 2,
                migrate = { prefs ->
                    prefs.string("should_run", "").set("executed")
                },
            ),
        )

        val success = migrationManager.migrate(2)

        assertTrue(success)
        assertEquals(2, migrationManager.getCurrentVersion())
        assertEquals("", datastore.string("should_not_run", "").get())
        assertEquals("executed", datastore.string("should_run", "").get())
    }

    @Test
    fun migrate_failsWithMigrationGap() = runTest(testDispatcher) {
        migrationManager.addMigrations(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { },
            ),
            PreferenceMigration(
                fromVersion = 2, // Gap: missing fromVersion = 1
                toVersion = 3,
                migrate = { },
            ),
        )

        val success = migrationManager.migrate(3)

        assertFalse(success)
        assertEquals(0, migrationManager.getCurrentVersion())
    }

    @Test
    fun migrate_failsWhenTargetNotReached() = runTest(testDispatcher) {
        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { },
            ),
        )

        val success = migrationManager.migrate(2) // No migration to version 2

        assertFalse(success)
    }

    @Test
    fun migrateIfNeeded_executesWhenNeeded() = runTest(testDispatcher) {
        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { },
            ),
        )

        val success = migrationManager.migrateIfNeeded(1)

        assertTrue(success)
        assertEquals(1, migrationManager.getCurrentVersion())
    }

    @Test
    fun migrateIfNeeded_skipsWhenNotNeeded() = runTest(testDispatcher) {
        migrationManager.resetVersion(1)

        val success = migrationManager.migrateIfNeeded(1)

        assertTrue(success)
        assertEquals(1, migrationManager.getCurrentVersion())
    }

    @Test
    fun migrationHelper_renameKey() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        // Set old key value
        datastore.string("old_name", "").set("test_value")

        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = MigrationHelpers.renameKey(
                    oldKey = "old_name",
                    newKey = "new_name",
                    defaultValue = "",
                    factory = { prefs, key, default -> prefs.string(key, default) },
                ),
            ),
        )

        migrationManager.migrate(1)

        assertEquals("", datastore.string("old_name", "").get())
        assertEquals("test_value", datastore.string("new_name", "").get())
    }

    @Test
    fun migrationHelper_transformValue() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        datastore.int("counter", 0).set(5)

        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = MigrationHelpers.transformValue(
                    key = "counter",
                    defaultValue = 0,
                    factory = { prefs, key, default -> prefs.int(key, default) },
                    transform = { it * 2 }, // Double the value
                ),
            ),
        )

        migrationManager.migrate(1)

        assertEquals(10, datastore.int("counter", 0).get())
    }

    @Test
    fun migrationHelper_removeKey() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        datastore.string("deprecated", "").set("old_value")

        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = MigrationHelpers.removeKey(
                    key = "deprecated",
                    defaultValue = "",
                    factory = { prefs, key, default -> prefs.string(key, default) },
                ),
            ),
        )

        migrationManager.migrate(1)

        assertEquals("", datastore.string("deprecated", "").get())
    }

    @Test
    fun migrationHelper_combineMigrations() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        datastore.string("key1", "").set("value1")
        datastore.string("key2", "").set("value2")

        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = MigrationHelpers.combineMigrations(
                    MigrationHelpers.renameKey(
                        oldKey = "key1",
                        newKey = "key1_new",
                        defaultValue = "",
                        factory = { prefs, key, default -> prefs.string(key, default) },
                    ),
                    MigrationHelpers.removeKey(
                        key = "key2",
                        defaultValue = "",
                        factory = { prefs, key, default -> prefs.string(key, default) },
                    ),
                ),
            ),
        )

        migrationManager.migrate(1)

        assertEquals("value1", datastore.string("key1_new", "").get())
        assertEquals("", datastore.string("key2", "").get())
    }

    @Test
    fun migration_withBatchOperations() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        // Set up old preferences
        val oldPrefs = (1..5).map { datastore.string("old_$it", "") }
        oldPrefs.forEachIndexed { index, pref ->
            pref.set("value_${index + 1}")
        }

        migrationManager.addMigration(
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { prefs ->
                    // Use batch get to read old values
                    val oldValues = prefs.batchGet(oldPrefs)

                    // Transform to new preferences
                    val newPrefs = (1..5).map { prefs.string("new_$it", "") }
                    val updates: Map<Prefs<*>, Any?> = newPrefs.mapIndexed { index, pref ->
                        pref to oldValues["old_${index + 1}"]
                    }.toMap()

                    // Use batch set to write new values
                    prefs.batchSet(updates)

                    // Use batch delete to remove old preferences
                    prefs.batchDelete(oldPrefs)
                },
            ),
        )

        migrationManager.migrate(1)

        // Verify old preferences are gone
        oldPrefs.forEach { pref ->
            assertEquals("", pref.get())
        }

        // Verify new preferences have correct values
        (1..5).forEach { i ->
            assertEquals("value_$i", datastore.string("new_$i", "").get())
        }
    }

    @Test
    fun migration_complexScenario() = runTest(testDispatcher) {
        val datastore = GenericPreferenceDatastore(dataStore)

        // V0: Initial state
        datastore.string("user_name", "").set("John")
        datastore.int("user_age", 0).set(30)
        datastore.string("theme", "").set("light")

        migrationManager.addMigrations(
            // V0 -> V1: Rename user_name to profile_name
            PreferenceMigration(
                fromVersion = 0,
                toVersion = 1,
                migrate = { prefs ->
                    val oldValue = prefs.string("user_name", "").get()
                    prefs.string("profile_name", "").set(oldValue)
                    prefs.string("user_name", "").delete()
                },
            ),
            // V1 -> V2: Add derived field, convert theme to enum-like int
            PreferenceMigration(
                fromVersion = 1,
                toVersion = 2,
                migrate = { prefs ->
                    // Add birth year (derived from age)
                    val age = prefs.int("user_age", 0).get()
                    val birthYear = 2024 - age
                    prefs.int("birth_year", 0).set(birthYear)

                    // Convert theme string to int (0=light, 1=dark)
                    val theme = prefs.string("theme", "").get()
                    val themeInt = if (theme == "dark") 1 else 0
                    prefs.int("theme_id", 0).set(themeInt)
                    prefs.string("theme", "").delete()
                },
            ),
            // V2 -> V3: Consolidate into single profile object
            PreferenceMigration(
                fromVersion = 2,
                toVersion = 3,
                migrate = { prefs ->
                    val name = prefs.string("profile_name", "").get()
                    val birthYear = prefs.int("birth_year", 0).get()
                    val themeId = prefs.int("theme_id", 0).get()

                    // Store as JSON-like string (in real app, use proper serialization)
                    val profile = "$name|$birthYear|$themeId"
                    prefs.string("user_profile", "").set(profile)

                    // Clean up old fields
                    prefs.batchDelete(
                        listOf(
                            prefs.string("profile_name", ""),
                            prefs.int("user_age", 0),
                            prefs.int("birth_year", 0),
                            prefs.int("theme_id", 0),
                        ),
                    )
                },
            ),
        )

        val success = migrationManager.migrate(3)

        assertTrue(success)
        assertEquals(3, migrationManager.getCurrentVersion())

        // Verify final state
        val profile = datastore.string("user_profile", "").get()
        assertEquals("John|1994|0", profile)

        // Verify old fields are gone
        assertEquals("", datastore.string("user_name", "").get())
        assertEquals("", datastore.string("profile_name", "").get())
        assertEquals(0, datastore.int("user_age", 0).get())
    }
}
