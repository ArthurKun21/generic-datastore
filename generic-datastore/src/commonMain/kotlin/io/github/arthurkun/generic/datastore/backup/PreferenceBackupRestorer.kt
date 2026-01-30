package io.github.arthurkun.generic.datastore.backup

import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

class PreferenceBackupRestorer(
    private val preferenceDatastore: PreferencesDatastore,
) {

    suspend fun restoreBackup(backup: PreferencesBackup) {
        val dataMap = backup.preferences.associate { pref ->
            pref.key to pref.value.getValue()
        }
        preferenceDatastore.import(dataMap)
    }

    suspend fun restoreFromJson(jsonString: String) {
        val backup = PreferencesBackup.fromJson(jsonString)
        restoreBackup(backup)
    }
}
