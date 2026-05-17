package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * [GenericPreferenceItem] for a [String] preference stored with `stringPreferencesKey`.
 */
internal class StringPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: String,
) : GenericPreferenceItem<String>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = stringPreferencesKey(key),
)
