package io.github.arthurkun.generic.datastore.compose.app

import io.github.arthurkun.generic.datastore.compose.app.domain.PreferenceStore

/**
 * Platform-specific container for app-wide dependencies.
 * Provides access to the PreferenceStore.
 */
expect class AppContainer {
    val preferenceStore: PreferenceStore
}

internal val preferenceName = "test.preferences_pb"
