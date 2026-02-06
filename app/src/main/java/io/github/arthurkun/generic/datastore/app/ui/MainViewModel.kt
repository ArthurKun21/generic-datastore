package io.github.arthurkun.generic.datastore.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.backup.PreferenceBackupCreator
import io.github.arthurkun.generic.datastore.backup.PreferenceBackupRestorer
import kotlinx.coroutines.launch

class MainViewModel(
    val preferenceStore: PreferenceStore,
) : ViewModel() {

    private val backupCreator = PreferenceBackupCreator(preferenceStore.datastore)
    private val backupRestorer = PreferenceBackupRestorer(preferenceStore.datastore)

    fun incrementLoginCount() = viewModelScope.launch {
        preferenceStore.num.update { it + 1 }
    }

    fun decrementLoginCount() = viewModelScope.launch {
        preferenceStore.num.update { it - 1 }
    }

    suspend fun exportPreferences(): String {
        return try {
            backupCreator.createBackupJson(
                includePrivatePreferences = false,
                includeAppStatePreferences = false,
            )
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to export preferences", e)
            throw e
        }
    }

    fun importPreferences(data: String) = viewModelScope.launch {
        try {
            backupRestorer.restoreFromJson(data)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to import preferences", e)
        }
    }
}
