package io.github.arthurkun.generic.datastore.compose.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.arthurkun.generic.datastore.compose.app.domain.Theme
import io.github.arthurkun.generic.datastore.compose.app.theme.GenericDataStoreAppTheme
import io.github.arthurkun.generic.datastore.compose.app.ui.MainScreen

fun main() {
    val appContainer = AppContainer()

    application {
        var useDarkTheme by remember { mutableStateOf<Boolean?>(null) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Generic DataStore Sample",
        ) {
            GenericDataStoreAppTheme(
                useDarkTheme = useDarkTheme,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainScreen(
                        preferenceStore = appContainer.preferenceStore,
                        onThemeChanged = { theme ->
                            useDarkTheme = when (theme) {
                                Theme.LIGHT -> false
                                Theme.DARK -> true
                                Theme.SYSTEM -> null
                            }
                        },
                    )
                }
            }
        }
    }
}
