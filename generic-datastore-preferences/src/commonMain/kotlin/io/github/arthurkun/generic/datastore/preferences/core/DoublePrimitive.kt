package io.github.arthurkun.generic.datastore.preferences.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey

/**
 * [GenericPreferenceItem] for a [Double] preference stored with `doublePreferencesKey`.
 */
internal class DoublePrimitive(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: Double,
) : GenericPreferenceItem<Double>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    preferences = doublePreferencesKey(key),
)
