package io.github.arthurkun.generic.datastore.preferences.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Wraps the current value into a [Flow] that emits the value or throws an exception if the
 * underlying data source encounters an error.
 */
internal val DataStore<Preferences>.dataOrEmpty: Flow<Preferences>
    get() = data
        .catch { error ->
            if (error is androidx.datastore.core.IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
