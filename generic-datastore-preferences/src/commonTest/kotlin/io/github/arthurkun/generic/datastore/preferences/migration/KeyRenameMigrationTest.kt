package io.github.arthurkun.generic.datastore.preferences.migration

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KeyRenameMigrationTest {

    @Test
    fun shouldMigrate_trueOnlyWhenOldKeyPresent() = runTest {
        val migration = KeyRenameMigration("old_name", "new_name")

        assertTrue(migration.shouldMigrate(preferencesOf(stringPreferencesKey("old_name") to "value")))
        assertTrue(!migration.shouldMigrate(preferencesOf(stringPreferencesKey("other") to "value")))
    }

    @Test
    fun migrate_renamesStringKey() = runTest {
        val migration = KeyRenameMigration("old_name", "new_name")

        val migrated = migration.migrate(preferencesOf(stringPreferencesKey("old_name") to "value"))

        assertEquals("value", migrated[stringPreferencesKey("new_name")])
        assertTrue(migrated.asMap().keys.none { it.name == "old_name" })
    }

    @Test
    fun migrate_renamesTypedKeys() = runTest {
        val intMigrated = KeyRenameMigration("old_int", "new_int")
            .migrate(preferencesOf(intPreferencesKey("old_int") to 5))
        assertEquals(5, intMigrated[intPreferencesKey("new_int")])

        val setMigrated = KeyRenameMigration("old_set", "new_set")
            .migrate(preferencesOf(stringSetPreferencesKey("old_set") to setOf("a", "b")))
        assertEquals(setOf("a", "b"), setMigrated[stringSetPreferencesKey("new_set")])

        val boolMigrated = KeyRenameMigration("old_bool", "new_bool")
            .migrate(preferencesOf(booleanPreferencesKey("old_bool") to true))
        assertEquals(true, boolMigrated[booleanPreferencesKey("new_bool")])
    }

    @Test
    fun migrate_doesNotOverwriteExistingTarget() = runTest {
        val migration = KeyRenameMigration("old_name", "new_name")

        val migrated = migration.migrate(
            preferencesOf(
                stringPreferencesKey("old_name") to "old-value",
                stringPreferencesKey("new_name") to "existing-value",
            ),
        )

        assertEquals("existing-value", migrated[stringPreferencesKey("new_name")])
        assertTrue(migrated.asMap().keys.none { it.name == "old_name" })
    }

    @Test
    fun migrate_withoutOldKeyIsNoOp() = runTest {
        val migration = KeyRenameMigration("old_name", "new_name")

        val migrated = migration.migrate(preferencesOf(stringPreferencesKey("other") to "value"))

        assertEquals("value", migrated[stringPreferencesKey("other")])
    }

    @Test
    fun constructor_rejectsInvalidKeys() {
        assertFailsWith<IllegalArgumentException> { KeyRenameMigration(" ", "new") }
        assertFailsWith<IllegalArgumentException> { KeyRenameMigration("old", " ") }
        assertFailsWith<IllegalArgumentException> { KeyRenameMigration("same", "same") }
    }

    @Test
    fun preferencesMigration_delegatesToLambdas() = runTest {
        var cleanedUp = false
        val migration = preferencesMigration(
            shouldMigrate = { it.asMap().keys.any { key -> key.name == "source" } },
            migrate = { current ->
                current.toMutablePreferences().apply {
                    val raw = current.asMap()
                    val old = raw.keys.first { it.name == "source" }
                    set(stringPreferencesKey("target"), raw[old] as String)
                    remove(old)
                }.toPreferences()
            },
            cleanUp = { cleanedUp = true },
        )

        assertTrue(migration.shouldMigrate(preferencesOf(stringPreferencesKey("source") to "v")))
        val migrated = migration.migrate(preferencesOf(stringPreferencesKey("source") to "v"))
        assertEquals("v", migrated[stringPreferencesKey("target")])

        migration.cleanUp()
        assertTrue(cleanedUp)
    }
}
