package io.github.arthurkun.generic.datastore.compose.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import okio.Path.Companion.toPath

actual class AppContainer(context: Context) {

    private fun createDataStore(producePath: () -> String): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { producePath().toPath() },
        )

    private fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
        producePath = { context.filesDir.resolve(preferenceName).absolutePath },
    )

    private val genericPreferenceDatastore = GenericPreferencesDatastore(
        datastore = createDataStore(context),
    )

    actual val preferenceStore = PreferenceStore(
        genericPreferenceDatastore,
    )
}
