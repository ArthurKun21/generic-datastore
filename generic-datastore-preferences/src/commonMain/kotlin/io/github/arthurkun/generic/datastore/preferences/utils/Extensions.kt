package io.github.arthurkun.generic.datastore.preferences.utils

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Returns the datastore data stream, substituting [emptyPreferences] for transient I/O failures.
 *
 * This keeps preference flows resilient to transient read errors while still rethrowing all
 * non-I/O exceptions. A [CorruptionException] is never swallowed: it propagates to the caller so
 * that on-disk corruption is distinguishable from a transient I/O error. To replace a corrupted
 * file automatically, register a `ReplaceFileCorruptionHandler` on `createPreferencesDatastore`.
 */
internal val DataStore<Preferences>.dataOrEmpty: Flow<Preferences>
    get() = data
        .catch { error ->
            when {
                error is CorruptionException -> throw error
                error is IOException -> emit(emptyPreferences())
                else -> throw error
            }
        }
