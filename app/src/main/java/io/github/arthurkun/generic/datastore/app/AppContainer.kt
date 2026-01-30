package io.github.arthurkun.generic.datastore.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import okio.Path.Companion.toPath

class AppContainer(context: Context) {

    // https://developer.android.com/kotlin/multiplatform/datastore#common
    fun createDataStore(producePath: () -> String): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { producePath().toPath() },
        )

    private val dataStoreFileName = "dice.preferences_pb"

    // https://developer.android.com/kotlin/multiplatform/datastore#android
    private fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
        producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath },
    )

    private val genericPreferenceDatastore = GenericPreferencesDatastore(
        datastore = createDataStore(context),
    )

    val preferenceStore = PreferenceStore(
        genericPreferenceDatastore,
    )
}
