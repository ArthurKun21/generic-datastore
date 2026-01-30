package io.github.arthurkun.generic.datastore.backup

import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

class PreferenceBackupCreator(
    private val preferenceDatastore: PreferencesDatastore,
) {

    suspend fun createBackup(
        includePrivatePreferences: Boolean = false,
        includeAppStatePreferences: Boolean = false,
    ): PreferencesBackup {
        val preferences = preferenceDatastore.export(
            exportPrivate = includePrivatePreferences,
            exportAppState = includeAppStatePreferences,
        )
        return PreferencesBackup(preferences = preferences)
    }

    suspend fun createBackupJson(
        includePrivatePreferences: Boolean = false,
        includeAppStatePreferences: Boolean = false,
    ): String {
        return createBackup(
            includePrivatePreferences = includePrivatePreferences,
            includeAppStatePreferences = includeAppStatePreferences,
        ).toJson()
    }
}
