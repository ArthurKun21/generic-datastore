package io.github.arthurkun.generic.datastore.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.app.domain.Theme
import kotlinx.coroutines.launch

class MainViewModel(
    private val preferenceStore: PreferenceStore,
) : ViewModel() {

    val theme = preferenceStore.theme.stateIn(viewModelScope)

    fun updateTheme(theme: Theme) = viewModelScope.launch {
        preferenceStore.theme.set(theme)
    }

    val text = preferenceStore.text.stateIn(viewModelScope)

    fun updateText(text: String) = viewModelScope.launch {
        preferenceStore.text.set(text)
    }

    val num = preferenceStore.num.stateIn(viewModelScope)

    fun updateNum(num: Int) = viewModelScope.launch {
        preferenceStore.num.set(num)
    }

    val bool = preferenceStore.bool.stateIn(viewModelScope)

    fun updateBool(bool: Boolean) = viewModelScope.launch {
        preferenceStore.bool.set(bool)
    }
}