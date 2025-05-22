package io.github.arthurkun.generic.datastore.app

import android.app.Application
import io.github.arthurkun.generic.datastore.app.domain.setAppCompatDelegateThemeMode

class MainApplication : Application() {

    val appContainer = AppContainer(this)

    override fun onCreate() {
        super.onCreate()

        val theme = appContainer.preferenceStore.theme.getValue()
        setAppCompatDelegateThemeMode(theme)
    }
}