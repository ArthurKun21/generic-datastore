package io.github.arthurkun.generic.datastore.preferences.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Returns the datastore data stream, substituting [emptyPreferences] for I/O failures.
 *
 * This keeps preference flows resilient to transient read errors while still rethrowing all
 * non-I/O exceptions.
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
