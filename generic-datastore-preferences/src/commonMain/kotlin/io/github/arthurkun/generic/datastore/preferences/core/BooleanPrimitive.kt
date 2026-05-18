package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * [GenericPreferenceItem] for a [Boolean] preference stored with `booleanPreferencesKey`.
 */
internal class BooleanPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Boolean,
) : GenericPreferenceItem<Boolean>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = booleanPreferencesKey(key),
)
