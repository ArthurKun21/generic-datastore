package io.github.arthurkun.generic.datastore.compose.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.arthurkun.generic.datastore.batch.LocalPreferencesDatastore
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.compose.app.ui.MainScreen
import io.github.arthurkun.generic.datastore.compose.app.ui.MainViewModel

@Composable
fun MainApp(
    preferenceStore: PreferenceStore,
) {
    CompositionLocalProvider(
        LocalPreferencesDatastore provides preferenceStore.datastore,
    ) {
        MainScreen(
            viewModel = MainViewModel(
                preferenceStore = preferenceStore,
            ),
        )
    }

}
