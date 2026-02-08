package io.github.arthurkun.generic.datastore.preferences.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.serialization.json.Json

/**
 * Internal utility class responsible for restoring [Preferences] from a [PreferencesBackup]
 * or its JSON string representation.
 *
 * This class handles the mapping between backup data models and actual Jetpack DataStore
 * preference keys, supporting standard types such as Int, Long, Float, Double, String,
 * Boolean, and String Sets.
 *
 * @property datastore The [DataStore] instance where the restored data will be written.
 */
internal class PreferenceBackupRestorer(private val datastore: DataStore<Preferences>) {

    suspend fun importData(backup: PreferencesBackup) {
        datastore.updateData { currentPreferences ->
            val mutablePreferences = currentPreferences.toMutablePreferences()

            backup.preferences.forEach { backupPref ->
                val backupKey = backupPref.key
                when (val backupValue = backupPref.value) {
                    is IntPreferenceValue -> mutablePreferences[intPreferencesKey(backupKey)] = backupValue.value

                    is LongPreferenceValue -> mutablePreferences[longPreferencesKey(backupKey)] = backupValue.value

                    is FloatPreferenceValue -> mutablePreferences[floatPreferencesKey(backupKey)] = backupValue.value

                    is DoublePreferenceValue -> mutablePreferences[doublePreferencesKey(backupKey)] = backupValue.value

                    is StringPreferenceValue -> mutablePreferences[stringPreferencesKey(backupKey)] = backupValue.value

                    is BooleanPreferenceValue -> mutablePreferences[booleanPreferencesKey(backupKey)] =
                        backupValue.value

                    is StringSetPreferenceValue -> mutablePreferences[stringSetPreferencesKey(backupKey)] =
                        backupValue.value
                }
            }

            mutablePreferences.toPreferences()
        }
    }

    suspend fun importDataAsString(
        backupString: String,
        json: Json,
    ) {
        val backup = json.decodeFromString<PreferencesBackup>(backupString)
        importData(backup)
    }
}
