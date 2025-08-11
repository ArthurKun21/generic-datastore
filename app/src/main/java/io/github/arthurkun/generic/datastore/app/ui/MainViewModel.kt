package io.github.arthurkun.generic.datastore.app.ui

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
        val newData = data.toJsonMap()
        preferenceStore.importPreferences(newData)
    }
}
