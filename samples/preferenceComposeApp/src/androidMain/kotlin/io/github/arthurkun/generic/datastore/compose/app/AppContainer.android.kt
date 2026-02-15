package io.github.arthurkun.generic.datastore.compose.app

import android.content.Context
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore

actual class AppContainer(context: Context) {

    private val genericPreferenceDatastore = createPreferencesDatastore(
        producePath = { context.filesDir.resolve(preferenceName).absolutePath },
    )

    actual val preferenceStore = PreferenceStore(
        genericPreferenceDatastore,
    )
}
