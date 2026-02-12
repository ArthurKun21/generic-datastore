package io.github.arthurkun.generic.datastore.proto.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.arthurkun.generic.datastore.proto.app.domain.AppContainer
import io.github.arthurkun.generic.datastore.proto.app.theme.ProtoAppTheme

fun main() {
    val appContainer = AppContainer()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Proto DataStore Sample",
        ) {
            ProtoAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProtoApp(appContainer)
                }
            }
        }
    }
}
