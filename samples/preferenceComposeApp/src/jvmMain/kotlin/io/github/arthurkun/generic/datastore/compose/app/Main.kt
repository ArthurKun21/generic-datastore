package io.github.arthurkun.generic.datastore.compose.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.arthurkun.generic.datastore.compose.app.domain.Theme
import io.github.arthurkun.generic.datastore.compose.app.theme.GenericDataStoreAppTheme
import io.github.arthurkun.generic.datastore.remember

fun main() {
    val appContainer = AppContainer()

    application {
        val darkTheme by appContainer.preferenceStore.theme.remember()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Generic DataStore Sample",
        ) {
            GenericDataStoreAppTheme(
                useDarkTheme = when (darkTheme) {
                    Theme.LIGHT -> false
                    Theme.DARK -> true
                    Theme.SYSTEM -> isSystemInDarkTheme()
                },
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainApp(appContainer.preferenceStore)
                }
            }
        }
    }
}
