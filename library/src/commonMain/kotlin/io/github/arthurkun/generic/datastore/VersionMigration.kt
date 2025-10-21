package io.github.arthurkun.generic.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Represents a migration from one version to another.
 *
 * Migrations are executed in order based on their version numbers to transform
 * preference data from an older schema to a newer one.
 *
 * @property fromVersion The version to migrate from
 * @property toVersion The version to migrate to
 * @property migrate Function that performs the migration on the datastore
 */
data class PreferenceMigration(
    val fromVersion: Int,
    val toVersion: Int,
    val migrate: suspend (GenericPreferenceDatastore) -> Unit,
) {
    init {
        require(toVersion > fromVersion) {
            "Migration toVersion ($toVersion) must be greater than fromVersion ($fromVersion)"
        }
    }
}

/**
 * Manager for handling versioned preference migrations.
 *
 * This class provides a structured way to migrate preference data across versions,
 * ensuring data consistency and backward compatibility as your app evolves.
 *
 * Example usage:
 * ```kotlin
 * val migrationManager = PreferenceMigrationManager(datastore)
 *
 * // Define migrations
 * migrationManager.addMigration(
 *     PreferenceMigration(
 *         fromVersion = 1,
 *         toVersion = 2,
 *         migrate = { prefs ->
 *             // Rename a preference
 *             val oldValue = prefs.string("old_key", "").get()
 *             prefs.string("new_key", "").set(oldValue)
 *             prefs.batchDelete(listOf(prefs.string("old_key", "")))
 *         }
 *     )
 * )
 *
 * // Run migrations
 * migrationManager.migrate(targetVersion = 2)
 * ```
 *
 * @property datastore The DataStore instance to migrate
 */
class PreferenceMigrationManager(
    private val datastore: DataStore<Preferences>,
) {
    private val preferenceDatastore = GenericPreferenceDatastore(datastore)
    private val migrations = mutableListOf<PreferenceMigration>()

    companion object {
        private const val VERSION_KEY = "__preference_version__"
        private val versionPreferenceKey = intPreferencesKey(VERSION_KEY)
    }

    /**
     * Adds a migration to the manager.
     *
     * Migrations should be added in the order they should be executed.
     * The manager will automatically order them by version.
     *
     * @param migration The migration to add
     * @return This manager instance for chaining
     */
    fun addMigration(migration: PreferenceMigration): PreferenceMigrationManager {
        migrations.add(migration)
        return this
    }

    /**
     * Adds multiple migrations at once.
     *
     * @param migrations List of migrations to add
     * @return This manager instance for chaining
     */
    fun addMigrations(vararg migrations: PreferenceMigration): PreferenceMigrationManager {
        this.migrations.addAll(migrations)
        return this
    }

    /**
     * Gets the current preference schema version.
     *
     * @return The current version, or 0 if no version is set
     */
    suspend fun getCurrentVersion(): Int {
        return try {
            datastore.data.first()[versionPreferenceKey] ?: 0
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to get current version", e)
            0
        }
    }

    /**
     * Sets the current preference schema version.
     *
     * @param version The version to set
     */
    private suspend fun setCurrentVersion(version: Int) {
        try {
            datastore.edit { prefs ->
                prefs[versionPreferenceKey] = version
            }
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to set version to $version", e)
        }
    }

    /**
     * Checks if migration is needed from current version to target version.
     *
     * @param targetVersion The target version to check against
     * @return True if migration is needed, false otherwise
     */
    suspend fun needsMigration(targetVersion: Int): Boolean {
        val currentVersion = getCurrentVersion()
        return currentVersion < targetVersion
    }

    /**
     * Executes all necessary migrations to reach the target version.
     *
     * This method will:
     * 1. Get the current version
     * 2. Find all migrations needed to reach the target version
     * 3. Execute them in order
     * 4. Update the version number
     *
     * Migrations are executed sequentially and atomically. If a migration fails,
     * the process stops and the version is not updated.
     *
     * @param targetVersion The version to migrate to
     * @return True if migration was successful or not needed, false if it failed
     */
    suspend fun migrate(targetVersion: Int): Boolean {
        val currentVersion = getCurrentVersion()

        if (currentVersion >= targetVersion) {
            ConsoleLogger.log("No migration needed. Current: $currentVersion, Target: $targetVersion")
            return true
        }

        // Sort migrations by fromVersion
        val sortedMigrations = migrations.sortedBy { it.fromVersion }

        // Find applicable migrations
        val applicableMigrations = sortedMigrations.filter {
            it.fromVersion >= currentVersion && it.toVersion <= targetVersion
        }

        if (applicableMigrations.isEmpty()) {
            ConsoleLogger.error(
                "No migration path from version $currentVersion to $targetVersion",
                null,
            )
            return false
        }

        // Validate migration chain
        var expectedVersion = currentVersion
        for (migration in applicableMigrations) {
            if (migration.fromVersion != expectedVersion) {
                ConsoleLogger.error(
                    "Migration gap detected: expected fromVersion $expectedVersion but got ${migration.fromVersion}",
                    null,
                )
                return false
            }
            expectedVersion = migration.toVersion
        }

        if (expectedVersion != targetVersion) {
            ConsoleLogger.error(
                "Migration chain doesn't reach target: ends at $expectedVersion but target is $targetVersion",
                null,
            )
            return false
        }

        // Execute migrations
        try {
            for (migration in applicableMigrations) {
                ConsoleLogger.log("Executing migration ${migration.fromVersion} -> ${migration.toVersion}")
                migration.migrate(preferenceDatastore)
                setCurrentVersion(migration.toVersion)
                ConsoleLogger.log("Migration ${migration.fromVersion} -> ${migration.toVersion} completed")
            }
            return true
        } catch (e: Exception) {
            ConsoleLogger.error("Migration failed", e)
            return false
        }
    }

    /**
     * Executes migrations automatically when needed.
     *
     * This is a convenience method that checks if migration is needed and executes it.
     *
     * @param targetVersion The target version
     * @return True if no migration was needed or migration succeeded, false if it failed
     */
    suspend fun migrateIfNeeded(targetVersion: Int): Boolean {
        return if (needsMigration(targetVersion)) {
            migrate(targetVersion)
        } else {
            true
        }
    }

    /**
     * Resets the version to a specific value.
     * Use with caution - mainly for testing purposes.
     *
     * @param version The version to reset to
     */
    suspend fun resetVersion(version: Int = 0) {
        setCurrentVersion(version)
    }
}

/**
 * Helper functions for common migration tasks.
 */
object MigrationHelpers {
    /**
     * Creates a migration that renames a preference key.
     *
     * @param oldKey The old preference key
     * @param newKey The new preference key
     * @param defaultValue Default value for the preference
     * @return A function that performs the rename migration
     */
    fun <T> renameKey(
        oldKey: String,
        newKey: String,
        defaultValue: T,
        factory: (GenericPreferenceDatastore, String, T) -> Prefs<T>,
    ): suspend (GenericPreferenceDatastore) -> Unit = { prefs ->
        val oldPref = factory(prefs, oldKey, defaultValue)
        val newPref = factory(prefs, newKey, defaultValue)

        val value = oldPref.get()
        if (value != defaultValue) {
            newPref.set(value)
        }
        oldPref.delete()
    }

    /**
     * Creates a migration that transforms a preference value.
     *
     * @param key The preference key
     * @param defaultValue Default value for the preference
     * @param transform Function to transform the old value to new value
     * @return A function that performs the transform migration
     */
    fun <T> transformValue(
        key: String,
        defaultValue: T,
        factory: (GenericPreferenceDatastore, String, T) -> Prefs<T>,
        transform: (T) -> T,
    ): suspend (GenericPreferenceDatastore) -> Unit = { prefs ->
        val pref = factory(prefs, key, defaultValue)
        val oldValue = pref.get()
        val newValue = transform(oldValue)
        pref.set(newValue)
    }

    /**
     * Creates a migration that removes a preference.
     *
     * @param key The preference key to remove
     * @param defaultValue Default value for the preference
     * @return A function that performs the deletion migration
     */
    fun <T> removeKey(
        key: String,
        defaultValue: T,
        factory: (GenericPreferenceDatastore, String, T) -> Prefs<T>,
    ): suspend (GenericPreferenceDatastore) -> Unit = { prefs ->
        val pref = factory(prefs, key, defaultValue)
        pref.delete()
    }

    /**
     * Creates a migration that combines multiple preference operations.
     *
     * @param operations List of migration operations to execute
     * @return A function that performs all operations in order
     */
    fun combineMigrations(
        vararg operations: suspend (GenericPreferenceDatastore) -> Unit,
    ): suspend (GenericPreferenceDatastore) -> Unit = { prefs ->
        operations.forEach { it(prefs) }
    }
}
