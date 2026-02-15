package io.github.arthurkun.generic.datastore.app

import android.app.Application
import io.github.arthurkun.generic.datastore.compose.app.AppContainer

class MainApplication : Application() {

    val appContainer = AppContainer(this)
}
