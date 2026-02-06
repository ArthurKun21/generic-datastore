package io.github.arthurkun.generic.datastore.compose.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import okio.Path.Companion.toPath
import java.io.File

actual class AppContainer {

    private fun createDataStore(): DataStore<Preferences> {
        val appDir = File(System.getProperty("user.home"), ".generic-datastore-sample")
        appDir.mkdirs()
        val filePath = File(appDir, preferenceName).absolutePath
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { filePath.toPath() },
        )
    }

    private val genericPreferenceDatastore = GenericPreferencesDatastore(
        datastore = createDataStore(),
    )

    actual val preferenceStore = PreferenceStore(
        genericPreferenceDatastore,
    )
}
