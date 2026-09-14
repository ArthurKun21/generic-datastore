package io.github.arthurkun.generic.datastore.preferences.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * A [DataMigration] that renames a preference key while keeping its stored value.
 *
 * When the datastore contains a value under [from], the value is copied to [to] (unless [to] is
 * already occupied) and the old key is removed. Pass the migration to
 * `createPreferencesDatastore(migrations = listOf(...))`; it runs once, before the first read.
 *
 * Prefer renaming keys in code with this migration over losing the stored values with a hard
 * key change.
 *
 * @param from The old key name.
 * @param to The new key name.
 */
public class KeyRenameMigration(
    private val from: String,
    private val to: String,
) : DataMigration<Preferences> {

    init {
        require(from.isNotBlank()) { "Migration source key cannot be blank." }
        require(to.isNotBlank()) { "Migration target key cannot be blank." }
        require(from != to) { "Migration source and target keys must differ." }
    }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.asMap().keys.any { it.name == from }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val raw = currentData.asMap()
        val oldKey = raw.keys.firstOrNull { it.name == from } ?: return currentData
        val value = raw[oldKey] ?: return currentData

        val mutable = currentData.toMutablePreferences()
        if (raw.keys.none { it.name == to }) {
            val newKey = newKeyFor(value) ?: return currentData
            @Suppress("UNCHECKED_CAST")
            mutable[newKey as Preferences.Key<Any>] = value
        }
        mutable.remove(oldKey)
        return mutable.toPreferences()
    }

    override suspend fun cleanUp() {
        // The old key is removed inside migrate(); nothing left to clean up.
    }

    private fun newKeyFor(value: Any): Preferences.Key<*> = when (value) {
        is Int -> intPreferencesKey(to)

        is Long -> longPreferencesKey(to)

        is Float -> floatPreferencesKey(to)

        is Double -> doublePreferencesKey(to)

        is String -> stringPreferencesKey(to)

        is Boolean -> booleanPreferencesKey(to)

        is Set<*> -> stringSetPreferencesKey(to)

        is ByteArray -> byteArrayPreferencesKey(to)

        else -> throw IllegalArgumentException(
            "KeyRenameMigration cannot migrate value of type ${value::class.simpleName}",
        )
    }
}

/**
 * Builds a custom [DataMigration] for Preferences DataStore with lambda hooks, mirroring the
 * androidx [DataMigration] contract:
 *
 * ```kotlin
 * val migration = preferencesMigration(
 *     shouldMigrate = { current -> current.asMap().keys.any { it.name == "old_counter" } },
 *     migrate = { current ->
 *         current.toMutablePreferences().apply {
 *             val raw = current.asMap()
 *             val old = raw.keys.first { it.name == "old_counter" }
 *             set(intPreferencesKey("new_counter"), (raw[old] as Long).toInt())
 *             remove(old)
 *         }.toPreferences()
 *     },
 * )
 * ```
 *
 * @param shouldMigrate Returns `true` while the datastore still needs this migration applied.
 * @param migrate Applies one migration step to the current preferences and returns the result.
 * @param cleanUp Runs once after the migration reports it no longer needs to run. Defaults to a
 *   no-op.
 */
public fun preferencesMigration(
    shouldMigrate: suspend (Preferences) -> Boolean,
    migrate: suspend (Preferences) -> Preferences,
    cleanUp: suspend () -> Unit = {},
): DataMigration<Preferences> = object : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean = shouldMigrate(currentData)

    override suspend fun migrate(currentData: Preferences): Preferences = migrate(currentData)

    override suspend fun cleanUp() = cleanUp()
}
