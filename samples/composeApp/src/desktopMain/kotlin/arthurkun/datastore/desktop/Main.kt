package arthurkun.datastore.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import arthurkun.datastore.common.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Generic Datastore Desktop Sample",
    ) {
        App()
    }
}
