package io.github.arthurkun.generic.datastore.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.arthurkun.generic.datastore.GenericPreferenceDatastore
import io.github.arthurkun.generic.datastore.desktop.domain.PreferenceStore
import io.github.arthurkun.generic.datastore.desktop.domain.Theme
import io.github.arthurkun.generic.datastore.desktop.ui.MainScreen
import io.github.arthurkun.generic.datastore.desktop.ui.MainViewModel
import okio.Path.Companion.toPath
import java.io.File

fun main() = application {
    val scope = rememberCoroutineScope()

    // Create DataStore
    val dataStore = remember {
        createDataStore()
    }

    // Create PreferenceStore
    val preferenceStore = remember {
        PreferenceStore(
            GenericPreferenceDatastore(dataStore),
        )
    }

    // Create ViewModel
    val viewModel = remember {
        MainViewModel(preferenceStore, scope)
    }

    // Observe theme changes
    var currentTheme by remember { mutableStateOf(Theme.SYSTEM) }

    LaunchedEffect(Unit) {
        preferenceStore.theme.asFlow().collect { theme ->
            currentTheme = theme
        }
    }

    // Determine if we should use dark theme
    val isDark = when (currentTheme) {
        Theme.DARK -> true
        Theme.LIGHT -> false
        Theme.SYSTEM -> false // Default to light for simplicity
    }

    val colorScheme = if (isDark) darkColorScheme() else lightColorScheme()

    Window(
        onCloseRequest = ::exitApplication,
        title = "GenericDataStore Desktop Sample",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Surface {
                MainScreen(vm = viewModel)
            }
        }
    }
}

private fun createDataStore(): DataStore<Preferences> {
    val dataStoreFile = File(
        System.getProperty("user.home"),
        ".genericdatastore/preferences.pb",
    ).also {
        it.parentFile?.mkdirs()
    }

    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStoreFile.absolutePath.toPath() },
    )
}
