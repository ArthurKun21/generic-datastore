package io.github.arthurkun.generic.datastore.app.ui

import androidx.compose.runtime.Composable
import io.github.arthurkun.generic.datastore.app.domain.setAppCompatDelegateThemeMode
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.compose.app.ui.MainScreen as SharedMainScreen

@Composable
fun MainScreen(
    preferenceStore: PreferenceStore,
) {
    SharedMainScreen(
        preferenceStore = preferenceStore,
        onThemeChanged = { theme ->
            setAppCompatDelegateThemeMode(theme)
        },
    )
}
