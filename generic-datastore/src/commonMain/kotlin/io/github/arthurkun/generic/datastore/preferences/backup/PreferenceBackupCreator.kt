package io.github.arthurkun.generic.datastore.preferences.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.core.BasePreference
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**
 * Internal exporter that snapshots a Preferences DataStore into [PreferencesBackup] data.
 *
 * Private keys and app-state keys can be filtered out before serialization.
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
            .asMap()
            .filter { (key, _) ->
                (exportPrivate || !BasePreference.isPrivate(key.name)) &&
                    (exportAppState || !BasePreference.isAppState(key.name))
            }
            .mapNotNull { (key, value) ->
                PreferenceValue.fromAny(value)?.let { preferenceValue ->
                    BackupPreference(key = key.name, value = preferenceValue)
                }
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
}
