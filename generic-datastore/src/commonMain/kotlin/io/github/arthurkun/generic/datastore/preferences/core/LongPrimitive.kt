package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey

/**
 * [GenericPreferenceItem] for a [Long] preference stored with `longPreferencesKey`.
 */
internal class LongPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Long,
) : GenericPreferenceItem<Long>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = longPreferencesKey(key),
)
