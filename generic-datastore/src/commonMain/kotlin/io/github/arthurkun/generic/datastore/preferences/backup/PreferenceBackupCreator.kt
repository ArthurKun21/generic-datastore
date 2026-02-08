package io.github.arthurkun.generic.datastore.preferences.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.Preference
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**
 * A utility class responsible for exporting [Preferences] from [DataStore] into a backup format.
 *
 * This class provides functionality to filter preferences based on their metadata (e.g., private
 * or app state) and can output the backup either as a structured [PreferencesBackup] object
 * or as a serialized JSON string.
 *
 * @property datastore The [DataStore] instance to read preferences from.
 */
internal class PreferenceBackupCreator(private val datastore: DataStore<Preferences>) {

    suspend fun exportAsData(
        exportPrivate: Boolean,
        exportAppState: Boolean,
    ): PreferencesBackup {
        val items = datastore
            .data
            .first()
            .toPreferences()
            .asMap()
            .mapNotNull { (key, value) ->
                if (!exportPrivate && Preference.isPrivate(key.name)) {
                    return@mapNotNull null
                }
                if (!exportAppState && Preference.isAppState(key.name)) {
                    return@mapNotNull null
                }
                val preferenceValue = value.toBackupPreferenceValue() ?: return@mapNotNull null
                BackupPreference(key = key.name, value = preferenceValue)
            }
        return PreferencesBackup(items)
    }

    suspend fun exportAsString(
        exportPrivate: Boolean,
        exportAppState: Boolean,
        json: Json,
    ): String {
        val backup = exportAsData(exportPrivate, exportAppState)
        return json.encodeToString<PreferencesBackup>(backup)
    }

    private fun Any?.toBackupPreferenceValue(): PreferenceValue? {
        return PreferenceValue.fromAny(this)
    }
}
