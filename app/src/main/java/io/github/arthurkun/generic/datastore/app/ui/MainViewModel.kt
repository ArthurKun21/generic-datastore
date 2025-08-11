package io.github.arthurkun.generic.datastore.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.toJsonMap
import kotlinx.coroutines.launch

class MainViewModel(
    val preferenceStore: PreferenceStore,
) : ViewModel() {

    suspend fun exportPreferences() = preferenceStore.exportPreferences()

    fun importPreferences(data: String) = viewModelScope.launch {
        try {
            val newData = data.toJsonMap()
            preferenceStore.importPreferences(newData)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to import preferences", e)
            throw e
        }
    }
}
