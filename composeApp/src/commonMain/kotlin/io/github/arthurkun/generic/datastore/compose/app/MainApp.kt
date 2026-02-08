package io.github.arthurkun.generic.datastore.compose.app

import androidx.compose.runtime.Composable
import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.compose.app.ui.MainScreen
import io.github.arthurkun.generic.datastore.compose.app.ui.MainViewModel

@Composable
fun MainApp(
    preferenceStore: PreferenceStore,
) {
    MainScreen(
        viewModel = MainViewModel(
            preferenceStore = preferenceStore,
        ),
    )
}
