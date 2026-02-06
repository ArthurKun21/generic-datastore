package io.github.arthurkun.generic.datastore.app.domain

import androidx.appcompat.app.AppCompatDelegate
import io.github.arthurkun.generic.datastore.compose.app.domain.Theme

fun setAppCompatDelegateThemeMode(themeMode: Theme) {
    AppCompatDelegate.setDefaultNightMode(
        when (themeMode) {
            Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        },
    )
}
