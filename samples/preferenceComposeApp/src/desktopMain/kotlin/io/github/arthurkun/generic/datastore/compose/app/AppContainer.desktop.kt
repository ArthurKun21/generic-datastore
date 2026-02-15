package io.github.arthurkun.generic.datastore.compose.app

import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import java.io.File

actual class AppContainer {

    private val genericPreferenceDatastore = createPreferencesDatastore(
        producePath = {
            val appDir = File(System.getProperty("user.home"), ".generic-datastore-sample")
            appDir.mkdirs()
            File(appDir, preferenceName).absolutePath
        },
    )

    actual val preferenceStore = PreferenceStore(
        genericPreferenceDatastore,
    )
}
