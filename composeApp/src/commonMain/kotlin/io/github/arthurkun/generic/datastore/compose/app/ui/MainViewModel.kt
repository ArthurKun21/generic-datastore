package io.github.arthurkun.generic.datastore.compose.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.compose.app.domain.Animal
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.compose.app.domain.Theme
import io.github.arthurkun.generic.datastore.compose.app.domain.UserProfile
import io.github.arthurkun.generic.datastore.preferences.toggle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainViewModel(
    private val preferenceStore: PreferenceStore,
) : ViewModel() {

    val datastore = preferenceStore.datastore

    val text = preferenceStore.text
    val num = preferenceStore.num
    val bool = preferenceStore.bool
    val customObject = preferenceStore.customObject
    val duration = preferenceStore.duration
    val userProfile = preferenceStore.userProfile
    val animalSet = preferenceStore.animalSet
    val theme = preferenceStore.theme
    val userProfileSet = preferenceStore.userProfileSet
    val themeSet = preferenceStore.themeSet

    private val jsonConfig = Json { prettyPrint = true }

    fun resetText() {
        viewModelScope.launch { preferenceStore.text.resetToDefault() }
    }

    fun resetNum() {
        viewModelScope.launch { preferenceStore.num.resetToDefault() }
    }

    fun resetBool() {
        viewModelScope.launch { preferenceStore.bool.resetToDefault() }
    }

    fun resetTheme() {
        viewModelScope.launch { preferenceStore.theme.resetToDefault() }
    }

    fun resetCustomObject() {
        viewModelScope.launch { preferenceStore.customObject.resetToDefault() }
    }

    fun resetDuration() {
        viewModelScope.launch { preferenceStore.duration.resetToDefault() }
    }

    fun resetUserProfile() {
        viewModelScope.launch { preferenceStore.userProfile.resetToDefault() }
    }

    fun resetUserProfileSet() {
        viewModelScope.launch { preferenceStore.userProfileSet.resetToDefault() }
    }

    fun addUserProfile(profile: UserProfile) {
        viewModelScope.launch { preferenceStore.userProfileSet.update { it + profile } }
    }

    fun removeUserProfile(profile: UserProfile) {
        viewModelScope.launch { preferenceStore.userProfileSet.update { it - profile } }
    }

    fun resetAnimalSet() {
        viewModelScope.launch { preferenceStore.animalSet.resetToDefault() }
    }

    fun toggleAnimal(animal: Animal) {
        viewModelScope.launch { preferenceStore.animalSet.toggle(animal) }
    }

    fun resetThemeSet() {
        viewModelScope.launch { preferenceStore.themeSet.resetToDefault() }
    }

    fun toggleTheme(theme: Theme) {
        viewModelScope.launch { preferenceStore.themeSet.toggle(theme) }
    }

    suspend fun exportPreferences(): String {
        return preferenceStore.exportPreferences(json = jsonConfig)
    }

    suspend fun importPreferences(backupString: String) {
        preferenceStore.importPreferences(backupString = backupString)
    }

    fun randomize() = viewModelScope.launch {
        preferenceStore.batchWriteBlock {
        }
    }
}
