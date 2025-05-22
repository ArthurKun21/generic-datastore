package io.github.arthurkun.generic.datastore.app

import android.app.Application

class MainApplication : Application() {

    val appContainer = AppContainer(this)
}