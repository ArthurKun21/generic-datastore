package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

/**
 * [GenericPreferenceItem] for an [Int] preference stored with `intPreferencesKey`.
 */
internal class IntPrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Int,
) : GenericPreferenceItem<Int>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = intPreferencesKey(key),
)
