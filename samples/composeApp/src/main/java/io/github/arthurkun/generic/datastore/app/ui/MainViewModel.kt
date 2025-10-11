package io.github.arthurkun.generic.datastore.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.toJsonElement
import io.github.arthurkun.generic.datastore.toJsonMap
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainViewModel(
    val preferenceStore: PreferenceStore,
) : ViewModel() {

    private val jsonConfig = Json { prettyPrint = true }

    suspend fun exportPreferences(): String {
        return try {
            val data = preferenceStore.exportPreferences()
            jsonConfig.encodeToString(
                data.toJsonElement(),
            )
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to export preferences", e)
            throw e
        }
    }

    fun importPreferences(data: String) = viewModelScope.launch {
        try {
            val newData = data.toJsonMap()
            preferenceStore.importPreferences(newData)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to import preferences", e)
        }
    }
}
