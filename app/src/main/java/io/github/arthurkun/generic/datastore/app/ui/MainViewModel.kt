package io.github.arthurkun.generic.datastore.app.ui

import androidx.lifecycle.ViewModel
import io.github.arthurkun.generic.datastore.app.domain.PreferenceStore

class MainViewModel(
    val preferenceStore: PreferenceStore,
) : ViewModel()