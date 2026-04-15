package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * [GenericPreferenceItem] for a `Set<String>` preference stored with `stringSetPreferencesKey`.
 */
internal class StringSetPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Set<String>,
) : GenericPreferenceItem<Set<String>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringSetPreferencesKey(key),
)
