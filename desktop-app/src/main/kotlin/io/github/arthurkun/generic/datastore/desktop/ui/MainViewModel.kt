package io.github.arthurkun.generic.datastore.desktop.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.arthurkun.generic.datastore.desktop.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.toJsonElement
import io.github.arthurkun.generic.datastore.toJsonMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainViewModel(
    val preferenceStore: PreferenceStore,
    private val scope: CoroutineScope,
) {

    private val jsonConfig = Json { prettyPrint = true }

    var isExporting by mutableStateOf(false)
        private set

    var isImporting by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun exportPreferences(): String {
        return try {
            val data = preferenceStore.exportPreferences()
            jsonConfig.encodeToString(
                data.toJsonElement(),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun importPreferences(data: String) = scope.launch {
        try {
            isImporting = true
            errorMessage = null
            val newData = data.toJsonMap()
            preferenceStore.importPreferences(newData)
        } catch (e: Exception) {
            errorMessage = "Failed to import preferences: ${e.message}"
        } finally {
            isImporting = false
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
