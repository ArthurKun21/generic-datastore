package io.github.arthurkun.generic.datastore.backup

import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

class PreferenceBackupRestorer(
    private val preferenceDatastore: PreferencesDatastore,
) {

    suspend fun restoreBackup(backup: PreferencesBackup) {
        preferenceDatastore.import(backup.preferences)
    }

    suspend fun restoreFromJson(jsonString: String) {
        val backup = PreferencesBackup.fromJson(jsonString)
        restoreBackup(backup)
    }
}
